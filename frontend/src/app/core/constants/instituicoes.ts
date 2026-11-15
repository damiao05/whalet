export interface Instituicao {
    chave: string,
    nome: string,
    cor: string,
}

export const INSTITUICOES: Instituicao[] = [
    { chave: 'nubank', nome: 'Nubank', cor: '#820AD1' },
    { chave: 'itau', nome: 'Itaú', cor: '#EC7000' },
    { chave: 'bradesco', nome: 'Bradesco', cor: '#CC092F' },
    { chave: 'bb', nome: 'Banco do Brasil', cor: '#F8D117' },
    { chave: 'santander', nome: 'Santander', cor: '#EC0000' },
    { chave: 'caixa', nome: 'Caixa', cor: '#0070AF' },
    { chave: 'inter', nome: 'Inter', cor: '#FF7A00' },
    { chave: 'c6', nome: 'C6 Bank', cor: '#2B2B2B' },
    { chave: 'picpay', nome: 'PicPay', cor: '#21C25E' },
    { chave: 'mercadopago', nome: 'Mercado Pago', cor: '#009EE3' },
];

export const CORES: string[] = [
  '#1263B5', '#12A594', '#16A34A', '#84CC16', '#F59E0B', '#F97316',
  '#EF4444', '#EC4899', '#8B5CF6', '#6366F1', '#64748B', '#0B2239',
];