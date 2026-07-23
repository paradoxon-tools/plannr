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

export type Contract = {
  id: number;
  pocketId: number;
  partnerId: number | null;
  signingDate: string | null;
  expirationDate: string | null;
  lastCancellationDate: string | null;
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
  nextCursor: string | null;
  hasMore: boolean;
};

export type UpcomingTransactionItem = {
  transactionTemplateId: number;
  occurrenceDate: string;
  sourcePocketId: number | null;
  destinationPocketId: number | null;
  partnerId: number | null;
  type: string;
  title: string;
  description: string | null;
  amount: number;
  currencyCode: string;
};

export type UpcomingTransactions = {
  afterDate: string;
  transactions: UpcomingTransactionItem[];
  hasMore: boolean;
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
  listContracts: (accountId: number) => request<Contract[]>(`/contracts?accountId=${accountId}`),
  getPocket: (id: number) => request<Pocket>(`/pockets/${id}`),
  getAccountFeed: (id: number, cursor?: string) =>
    request<TransactionFeed>(`/accounts/${id}/feed${cursorQuery(cursor)}`),
  getPocketFeed: (id: number, cursor?: string) =>
    request<TransactionFeed>(`/pockets/${id}/feed${cursorQuery(cursor)}`),
  getContractFeed: (id: number, cursor?: string) =>
    request<TransactionFeed>(`/contracts/${id}/feed${cursorQuery(cursor)}`),
  getUpcomingAccountTransactions: (id: number, after?: string, count = 50) =>
    request<UpcomingTransactions>(`/accounts/${id}/upcoming-transactions${upcomingQuery(after, count)}`),
  getUpcomingPocketTransactions: (id: number, after?: string, count = 50) =>
    request<UpcomingTransactions>(`/pockets/${id}/upcoming-transactions${upcomingQuery(after, count)}`),
  getUpcomingContractTransactions: (id: number, after?: string, count = 50) =>
    request<UpcomingTransactions>(`/contracts/${id}/upcoming-transactions${upcomingQuery(after, count)}`),
};

function cursorQuery(cursor?: string): string {
  return cursor ? `?cursor=${encodeURIComponent(cursor)}` : '';
}

function upcomingQuery(after: string | undefined, count: number): string {
  const parameters = [`count=${count}`];
  if (after) {
    parameters.unshift(`after=${encodeURIComponent(after)}`);
  }
  return `?${parameters.join('&')}`;
}

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
