import type { VercelRequest, VercelResponse } from '@vercel/node';
import mysql from 'mysql2/promise';
import jwt from 'jsonwebtoken';
import { randomUUID } from 'node:crypto';

type Auth = { user: string };
const db = mysql.createPool({
  host: process.env.DB_HOST, port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER, password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME || 'turnopro_db', waitForConnections: true,
  connectionLimit: 3, ssl: { ca: process.env.DB_SSL_CA || undefined, rejectUnauthorized: true },
  timezone: 'Z', dateStrings: true,
});
const secret = process.env.JWT_SECRET || '';
const json = (res: VercelResponse, status: number, body: unknown) => res.status(status).json(body);
const dateToday = () => new Date().toISOString().slice(0, 10);
function auth(req: VercelRequest): Auth | null {
  const header = req.headers.authorization || '';
  if (!secret || !header.startsWith('Bearer ')) return null;
  try { return jwt.verify(header.slice(7), secret) as Auth; } catch { return null; }
}
const query = async <T = any>(sql: string, values: unknown[] = []) => {
  const [rows] = await db.execute(sql, values);
  return rows as T;
};
function view(row: any, ahead = 0) {
  return { id: row.id, number: row.numero_turno, service: row.servicio, status: row.estado,
    ahead, estimatedMinutes: ahead * Number(row.tiempo_promedio_min || 0), createdAt: row.creado_en,
    consultationCode: row.codigo_consulta, counter: row.ventanilla_id ? `Ventanilla ${row.ventanilla_id}` : null, name: row.cliente_nombre };
}
async function ticketById(id: number) {
  const rows = await query<any[]>(`SELECT t.*,s.nombre servicio,s.tiempo_promedio_min FROM turnos t JOIN servicios s ON s.id=t.servicio_id WHERE t.id=?`, [id]);
  if (!rows.length) throw Object.assign(new Error('Turno no encontrado.'), { status: 404 });
  const r = rows[0];
  const a = await query<any[]>(`SELECT COUNT(*) n FROM turnos WHERE fecha_operativa=? AND servicio_id=? AND estado IN ('ESPERA','LLAMADO') AND (prioridad>? OR (prioridad=? AND numero_secuencial<?))`, [r.fecha_operativa,r.servicio_id,r.prioridad,r.prioridad,r.numero_secuencial]);
  return view(r, Number(a[0].n));
}
async function transition(id: number, action: string, user: string) {
  const states: Record<string, [string,string]> = {
    llamar: ['ESPERA','LLAMADO'], iniciar: ['LLAMADO','EN_ATENCION'], finalizar: ['EN_ATENCION','ATENDIDO'],
    ausente: ['LLAMADO','AUSENTE'], cancelar: ['', 'CANCELADO'],
  };
  const step = states[action]; if (!step) throw Object.assign(new Error('Acción no válida.'), { status: 400 });
  const conn = await db.getConnection();
  try {
    await conn.beginTransaction();
    const [found] = await conn.execute<any[]>('SELECT estado FROM turnos WHERE id=? FOR UPDATE', [id]);
    if (!found.length) throw Object.assign(new Error('Turno no encontrado.'), { status: 404 });
    const old = found[0].estado;
    if ((step[0] && old !== step[0]) || (action === 'cancelar' && ['ATENDIDO','CANCELADO'].includes(old))) throw Object.assign(new Error('El turno no puede realizar esa transición.'), { status: 409 });
    const timestamp = action === 'llamar' ? ', llamado_en=UTC_TIMESTAMP()' : action === 'iniciar' ? ', inicio_atencion=UTC_TIMESTAMP()' : action === 'finalizar' ? ', fin_atencion=UTC_TIMESTAMP()' : '';
    await conn.execute(`UPDATE turnos SET estado=?${timestamp} WHERE id=?`, [step[1], id]);
    await conn.execute('INSERT INTO historial_turnos(turno_id,estado_anterior,estado_nuevo,detalle) VALUES(?,?,?,?)', [id,old,step[1],`Acción ${action} por ${user}`]);
    await conn.commit();
  } catch (e) { await conn.rollback(); throw e; } finally { conn.release(); }
  return ticketById(id);
}

export default async function handler(req: VercelRequest, res: VercelResponse) {
  res.setHeader('Cache-Control', 'no-store');
  if (req.method === 'OPTIONS') return res.status(204).end();
  const route = (req.query.path as string[] || []).join('/');
  try {
    if (route === 'health' && req.method === 'GET') { await query('SELECT 1'); return json(res,200,{status:'UP'}); }
    if (route === 'servicios' && req.method === 'GET') return json(res,200,await query(`SELECT id,nombre name,descripcion description,prefijo prefix,tiempo_promedio_min averageMinutes FROM servicios WHERE activo=1 ORDER BY orden_visualizacion`));
    if (route === 'auth/login' && req.method === 'POST') {
      const { username, password } = req.body || {};
      const expected = process.env.ADMIN_INITIAL_PASSWORD || '';
      if (!username || username !== process.env.ADMIN_INITIAL_USERNAME || !expected || String(password || '') !== expected) return json(res,401,{message:'Credenciales no válidas.'});
      const token = jwt.sign({user:username,role:'ADMINISTRADOR'},secret,{expiresIn:'12h'});
      return json(res,200,{token,tokenType:'Bearer'});
    }
    if (route === 'auth/logout' && req.method === 'POST') return json(res,200,{message:'Sesión cerrada en el cliente.'});
    if (route === 'turnos' && req.method === 'POST') {
      const { name, serviceId, phone } = req.body || {};
      if (!String(name || '').trim() || String(name).length>100 || !Number(serviceId)) return json(res,400,{message:'Nombre y servicio son obligatorios.'});
      const services = await query<any[]>(`SELECT * FROM servicios WHERE id=? AND activo=1`,[Number(serviceId)]);
      if (!services.length) return json(res,400,{message:'Servicio no disponible.'});
      const s=services[0], day=dateToday(), conn=await db.getConnection(); let id:number;
      try {
        await conn.beginTransaction();
        await conn.execute('SELECT id FROM servicios WHERE id=? FOR UPDATE',[s.id]);
        const [max] = await conn.execute<any[]>('SELECT COALESCE(MAX(numero_secuencial),0)+1 seq FROM turnos WHERE servicio_id=? AND fecha_operativa=?',[s.id,day]);
        const seq=Number(max[0].seq), number=`${s.prefijo}${String(seq).padStart(3,'0')}`, code=randomUUID()+randomUUID();
        const [result] = await conn.execute<any>('INSERT INTO turnos(numero_turno,numero_secuencial,fecha_operativa,servicio_id,sucursal_id,cliente_nombre,cliente_telefono,codigo_consulta,creado_en) VALUES(?,?,?,?,1,?,?,?,UTC_TIMESTAMP())',[number,seq,day,s.id,String(name).trim(),phone||null,code]);
        id=result.insertId;
        await conn.execute(`INSERT INTO historial_turnos(turno_id,estado_anterior,estado_nuevo,detalle) VALUES(?,NULL,'ESPERA','Solicitud pública')`,[id]);
        await conn.commit();
      } catch(e) { await conn.rollback(); throw e; } finally { conn.release(); }
      return json(res,201,await ticketById(id));
    }
    if (route.startsWith('turnos/consulta/') && req.method==='GET') {
      const code=route.slice('turnos/consulta/'.length), number=String(req.query.number||'');
      const found=await query<any[]>(`SELECT t.id FROM turnos t WHERE t.codigo_consulta=? AND t.numero_turno=?`,[code,number]);
      if (!found.length) return json(res,404,{message:'No se encontró el turno.'});
      return json(res,200,await ticketById(found[0].id));
    }
    if (route === 'pantalla/llamados' && req.method==='GET') {
      const rows=await query<any[]>(`SELECT t.*,s.nombre servicio,s.tiempo_promedio_min FROM turnos t JOIN servicios s ON s.id=t.servicio_id WHERE t.estado IN ('LLAMADO','EN_ATENCION') AND t.fecha_operativa=? ORDER BY t.llamado_en DESC LIMIT 8`,[dateToday()]);
      return json(res,200,rows.map(r=>view(r)));
    }
    const actionRoute=route.match(/^turnos\/(\d+)\/([a-z]+)$/);
    if (actionRoute && actionRoute[2]==='historial' && req.method==='GET') {
      if (!auth(req)) return json(res,401,{message:'Se requiere iniciar sesión.'});
      return json(res,200,await query(`SELECT estado_anterior `from`,estado_nuevo `to`,detalle detail,creado_en createdAt FROM historial_turnos WHERE turno_id=? ORDER BY creado_en`,[Number(actionRoute[1])]));
    }
    if (actionRoute && req.method==='POST') {
      const logged=auth(req); if (!logged) return json(res,401,{message:'Se requiere iniciar sesión.'});
      return json(res,200,await transition(Number(actionRoute[1]),actionRoute[2],logged.user));
    }
    if (route==='turnos/espera' && req.method==='GET') {
      if (!auth(req)) return json(res,401,{message:'Se requiere iniciar sesión.'});
      const rows=await query<any[]>(`SELECT t.*,s.nombre servicio,s.tiempo_promedio_min FROM turnos t JOIN servicios s ON s.id=t.servicio_id WHERE t.estado='ESPERA' AND t.fecha_operativa=? ORDER BY t.prioridad DESC,t.creado_en`,[dateToday()]);
      const content=[] as any[]; for (const r of rows) content.push(await ticketById(r.id));
      return json(res,200,{content});
    }
    if (route==='reportes/resumen' && req.method==='GET') {
      if (!auth(req)) return json(res,401,{message:'Se requiere iniciar sesión.'});
      const [row]=await query<any[]>(`SELECT COUNT(*) today,SUM(estado='ESPERA') waiting,SUM(estado='EN_ATENCION') inProgress,SUM(estado='ATENDIDO') completed,COALESCE(AVG(TIMESTAMPDIFF(MINUTE,creado_en,inicio_atencion)),0) avgWait,COALESCE(AVG(TIMESTAMPDIFF(MINUTE,inicio_atencion,fin_atencion)),0) avgService FROM turnos WHERE fecha_operativa=?`,[dateToday()]);
      return json(res,200,row);
    }
    return json(res,404,{message:'Ruta no encontrada.'});
  } catch (error:any) {
    console.error('API error:',error);
    return json(res,error.status||500,{message:error.status?error.message:'Error interno del servidor.'});
  }
}
