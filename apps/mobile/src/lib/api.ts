import { getApiBaseUrl } from './settings';

export type Account = {
  id: number;
  name: string;
  institution: string;
  currencyCode: string;
  weekendHandling: string;
  isArchived: boolean;
  createdAt: number;
};

export type Pocket = {
  id: number;
  accountId: number;
  name: string;
  description: string | null;
  color: number;
  isDefault: boolean;
  isContractPocket: boolean;
  isArchived: boolean;
  createdAt: number;
  currentBalance?: number | null;
};

export type ContractInfo = {
  partnerId: number | null;
  signingDate: string | null;
  expirationDate: string | null;
  lastCancellationDate: string | null;
};

export type PocketWithContract = Pocket & {
  contractInfo: ContractInfo;
};

async function request<T>(path: string): Promise<T> {
  const baseUrl = await getApiBaseUrl();
  const response = await fetch(`${baseUrl}${path}`);

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`${response.status} ${response.statusText}${body ? `: ${body}` : ''}`);
  }

  return response.json() as Promise<T>;
}

export const api = {
  listAccounts: () => request<Account[]>('/accounts'),
  getAccount: (id: number) => request<Account>(`/accounts/${id}`),
  listPockets: (accountId: number) => request<Pocket[]>(`/pockets?accountId=${accountId}`),
  listContracts: (accountId: number) => request<PocketWithContract[]>(`/contracts?accountId=${accountId}`),
  getPocket: (id: number) => request<Pocket>(`/pockets/${id}`),
};

export function centsToMoney(cents: number | null | undefined, currencyCode: string): string {
  if (typeof cents !== 'number') {
    return 'Not exposed by API';
  }

  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency: currencyCode,
  }).format(cents / 100);
}

export function colorFromInt(value: number): string {
  const normalized = value < 0 ? value + 0x100000000 : value;
  return `#${(normalized & 0xffffff).toString(16).padStart(6, '0')}`;
}
