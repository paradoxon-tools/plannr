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

export type TransactionFeedReference = {
  id: number;
  name: string;
  color: number | null;
};

export type TransactionFeedItem = {
  transactionId: number;
  transactionTemplateId: number;
  historyPosition: number;
  transactionDate: string;
  type: string;
  title: string;
  description: string | null;
  transactionAmount: number;
  signedAmount: number;
  balanceAfter: number;
  partner: TransactionFeedReference | null;
  sourcePocket: TransactionFeedReference | null;
  destinationPocket: TransactionFeedReference | null;
  transferPocket: TransactionFeedReference | null;
  isArchived: boolean;
};

export type TransactionFeed = {
  currentBalance: number;
  transactions: TransactionFeedItem[];
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
  getAccountFeed: (id: number) => request<TransactionFeed>(`/accounts/${id}/feed`),
  getPocketFeed: (id: number) => request<TransactionFeed>(`/pockets/${id}/feed`),
  getContractFeed: (id: number) => request<TransactionFeed>(`/contracts/${id}/feed`),
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
