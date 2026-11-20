const pad = (n: number) => String(n).padStart(2, '0');

export function hojeIso(): string {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
export function mesAtual(): string {
  return hojeIso().slice(0, 7);
}
export function deslocarMes(mes: string, delta: number): string {
  const [ano, m] = mes.split('-').map(Number);
  const d = new Date(ano, m - 1 + delta, 1);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`;
}
export function deslocarDias(iso: string, dias: number): string {
  const [a, m, d] = iso.split('-').map(Number);
  const x = new Date(a, m - 1, d + dias);
  return `${x.getFullYear()}-${pad(x.getMonth() + 1)}-${pad(x.getDate())}`;
}
export function rotuloMes(mes: string): string {
  const [ano, m] = mes.split('-').map(Number);
  return new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(new Date(ano, m - 1, 1));
}
export function dataCurta(iso: string): string {
  const [, m, d] = iso.split('-');
  return `${d}/${m}`;
}
