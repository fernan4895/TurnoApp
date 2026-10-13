import axios from 'axios';
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080/api' : '/api'), timeout: 20000 });
api.interceptors.request.use(config => { const token = sessionStorage.getItem('turnopro-token'); if (token) config.headers.Authorization = `Bearer ${token}`; return config; });
export type Service = { id: number; name: string; description: string; prefix: string; averageMinutes: number };
export type Ticket = { id: number; number: string; service: string; status: string; ahead: number; estimatedMinutes: number; createdAt: string; consultationCode?: string };
export const services: Service[] = [{id:1,name:'Atención general',description:'Trámites y atención personalizada',prefix:'A',averageMinutes:12},{id:2,name:'Caja',description:'Pagos, cobros y facturación',prefix:'C',averageMinutes:8},{id:3,name:'Informes',description:'Orientación y consultas',prefix:'I',averageMinutes:6},{id:4,name:'Soporte técnico',description:'Ayuda con productos y servicios',prefix:'S',averageMinutes:18}];
export async function createTicket(name: string, serviceId: number, phone?: string): Promise<Ticket> { const {data}=await api.post('/turnos',{name,serviceId,phone}); return data; }
export async function lookupTicket(number: string, code: string): Promise<Ticket> { const {data}=await api.get(`/turnos/consulta/${encodeURIComponent(code)}`,{params:{number}}); return data; }
