import type {
  Account,
  AccountForm,
  ContractForm,
  Currency,
  CurrencyForm,
  PartnerForm,
  PocketForm,
  RecurringForm,
  RecurringTransaction
} from '$lib/types';
import { colorPalette } from '$lib/types';

export function today() {
  return new Date().toISOString().slice(0, 10);
}

export function createEmptyAccountForm(currencyCode = 'EUR'): AccountForm {
  return {
    id: null,
    name: '',
    institution: '',
    currencyCode,
    weekendHandling: 'next_business_day'
  };
}

export function createEmptyPocketForm(accountId = ''): PocketForm {
  return {
    id: null,
    accountId,
    name: '',
    description: '',
    color: colorPalette[0],
    isDefault: false
  };
}

export function createEmptyPartnerForm(): PartnerForm {
  return { id: null, name: '', notes: '' };
}

export function createEmptyContractForm(accountId = ''): ContractForm {
  return {
    id: null,
    accountId,
    pocketId: '',
    partnerId: '',
    name: '',
    startDate: today(),
    endDate: '',
    notes: ''
  };
}

export function createEmptyRecurringForm(currencyCode = 'EUR'): RecurringForm {
  return {
    id: null,
    updateMode: 'overwrite',
    effectiveFromDate: '',
    contractId: '',
    sourcePocketId: '',
    destinationPocketId: '',
    partnerId: '',
    title: '',
    description: '',
    amount: 0,
    currencyCode,
    transactionType: 'expense',
    firstOccurrenceDate: today(),
    finalOccurrenceDate: '',
    recurrenceType: 'monthly',
    skipCount: 0,
    daysOfWeek: [],
    weeksOfMonth: [],
    daysOfMonth: [1],
    monthsOfYear: []
  };
}

export function createEmptyCurrencyForm(code = 'EUR'): CurrencyForm {
  return {
    code,
    originalCode: null,
    name: '',
    symbol: '',
    decimalPlaces: 2,
    symbolPosition: 'before'
  };
}

export function dedupeBy<T>(items: T[], getKey: (item: T) => string) {
  const map = new Map<string, T>();
  for (const item of items) map.set(getKey(item), item);
  return [...map.values()];
}

export function sortByCreatedAt<T extends { createdAt?: number }>(items: T[]) {
  return [...items].sort((a, b) => (a.createdAt ?? 0) - (b.createdAt ?? 0));
}

export function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Something went wrong';
}

export function normalizeOptionalString(value: string) {
  const trimmed = value.trim();
  return trimmed.length ? trimmed : null;
}

export function normalizedArray<T>(items: T[]) {
  return items.length ? items : null;
}

export function toggleString(list: string[], value: string) {
  return list.includes(value) ? list.filter((item) => item !== value) : [...list, value];
}

export function toggleNumber(list: number[], value: number) {
  return list.includes(value) ? list.filter((item) => item !== value) : [...list, value].sort((a, b) => a - b);
}

export function formatCreatedAt(timestamp: number) {
  return new Date(timestamp).toLocaleString();
}

export function hexColor(color: number) {
  return `#${color.toString(16).padStart(6, '0')}`;
}

export function formatMoney(amount: number, currencyCode: string, currencies: Currency[]) {
  const currency = currencies.find((item) => item.code === currencyCode);
  const decimals = currency?.decimalPlaces ?? 2;
  const value = amount / 10 ** decimals;
  const formatted = new Intl.NumberFormat(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(value);

  if (!currency) return `${formatted} ${currencyCode}`;
  return currency.symbolPosition === 'before' ? `${currency.symbol}${formatted}` : `${formatted}${currency.symbol}`;
}

export function recurrenceSummary(item: RecurringTransaction) {
  const parts = [item.recurrenceType];
  if (item.skipCount > 0) parts.push(`skip ${item.skipCount}`);
  if (item.daysOfWeek?.length) parts.push(item.daysOfWeek.join(', '));
  if (item.weeksOfMonth?.length) parts.push(`weeks ${item.weeksOfMonth.join(', ')}`);
  if (item.daysOfMonth?.length) parts.push(`days ${item.daysOfMonth.join(', ')}`);
  if (item.monthsOfYear?.length) parts.push(`months ${item.monthsOfYear.join(', ')}`);
  return parts.join(' · ');
}

export function accountDisplayName(accountId: string, accounts: Account[]) {
  const account = accounts.find((item) => item.id === accountId);
  return account ? `${account.name} · ${account.institution}` : accountId;
}
