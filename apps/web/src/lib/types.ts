export type SectionId =
  | 'overview'
  | 'accounts'
  | 'pockets'
  | 'contracts'
  | 'recurring'
  | 'partners'
  | 'currencies';

export type Account = {
  id: string;
  name: string;
  institution: string;
  currencyCode: string;
  weekendHandling: string;
  isArchived: boolean;
  createdAt: number;
};

export type Pocket = {
  id: string;
  accountId: string;
  name: string;
  description: string | null;
  color: number;
  isDefault: boolean;
  isArchived: boolean;
  createdAt: number;
};

export type Partner = {
  id: string;
  name: string;
  notes: string | null;
  isArchived: boolean;
  createdAt: number;
};

export type Contract = {
  id: string;
  accountId: string;
  pocketId: string;
  partnerId: string | null;
  name: string;
  startDate: string;
  endDate: string | null;
  notes: string | null;
  isArchived: boolean;
  createdAt: number;
};

export type RecurringTransaction = {
  id: string;
  contractId: string | null;
  accountId: string;
  sourcePocketId: string | null;
  destinationPocketId: string | null;
  partnerId: string | null;
  title: string;
  description: string | null;
  amount: number;
  currencyCode: string;
  transactionType: string;
  firstOccurrenceDate: string;
  finalOccurrenceDate: string | null;
  recurrenceType: string;
  skipCount: number;
  daysOfWeek: string[] | null;
  weeksOfMonth: number[] | null;
  daysOfMonth: number[] | null;
  monthsOfYear: number[] | null;
  lastMaterializedDate: string | null;
  previousVersionId: string | null;
  isArchived: boolean;
  createdAt: number;
};

export type Currency = {
  code: string;
  name: string;
  symbol: string;
  decimalPlaces: number;
  symbolPosition: string;
};

export type ApiErrorEnvelope = {
  error?: {
    code?: string;
    message?: string;
    details?: Record<string, unknown>;
  };
};

export type Notice = {
  tone: 'success' | 'error' | 'info';
  message: string;
} | null;

export type AccountForm = {
  id: string | null;
  name: string;
  institution: string;
  currencyCode: string;
  weekendHandling: string;
};

export type PocketForm = {
  id: string | null;
  accountId: string;
  name: string;
  description: string;
  color: number;
  isDefault: boolean;
};

export type PartnerForm = {
  id: string | null;
  name: string;
  notes: string;
};

export type ContractForm = {
  id: string | null;
  accountId: string;
  pocketId: string;
  partnerId: string;
  name: string;
  startDate: string;
  endDate: string;
  notes: string;
};

export type RecurringForm = {
  id: string | null;
  updateMode: string;
  effectiveFromDate: string;
  contractId: string;
  sourcePocketId: string;
  destinationPocketId: string;
  partnerId: string;
  title: string;
  description: string;
  amount: number;
  currencyCode: string;
  transactionType: string;
  firstOccurrenceDate: string;
  finalOccurrenceDate: string;
  recurrenceType: string;
  skipCount: number;
  daysOfWeek: string[];
  weeksOfMonth: number[];
  daysOfMonth: number[];
  monthsOfYear: number[];
};

export type CurrencyForm = {
  code: string;
  originalCode: string | null;
  name: string;
  symbol: string;
  decimalPlaces: number;
  symbolPosition: string;
};

export const sections: { id: SectionId; label: string }[] = [
  { id: 'overview', label: 'Overview' },
  { id: 'accounts', label: 'Accounts' },
  { id: 'pockets', label: 'Pockets' },
  { id: 'contracts', label: 'Contracts' },
  { id: 'recurring', label: 'Recurring' },
  { id: 'partners', label: 'Partners' },
  { id: 'currencies', label: 'Currencies' }
];

export const weekendHandlingOptions = [
  { value: 'same_day', label: 'Same day' },
  { value: 'next_business_day', label: 'Next business day' },
  { value: 'previous_business_day', label: 'Previous business day' }
];

export const symbolPositions = [
  { value: 'before', label: 'Before amount' },
  { value: 'after', label: 'After amount' }
];

export const transactionTypes = [
  { value: 'expense', label: 'Expense' },
  { value: 'income', label: 'Income' },
  { value: 'transfer', label: 'Transfer' }
];

export const recurrenceTypes = [
  { value: 'none', label: 'One-time' },
  { value: 'daily', label: 'Daily' },
  { value: 'weekly', label: 'Weekly' },
  { value: 'monthly', label: 'Monthly' },
  { value: 'yearly', label: 'Yearly' }
];

export const updateModes = [
  { value: 'overwrite', label: 'Overwrite series' },
  { value: 'effective_from', label: 'Effective from date' },
  { value: 'parallel', label: 'Create parallel version' }
];

export const daysOfWeekOptions = [
  { value: 'MONDAY', label: 'Mon' },
  { value: 'TUESDAY', label: 'Tue' },
  { value: 'WEDNESDAY', label: 'Wed' },
  { value: 'THURSDAY', label: 'Thu' },
  { value: 'FRIDAY', label: 'Fri' },
  { value: 'SATURDAY', label: 'Sat' },
  { value: 'SUNDAY', label: 'Sun' }
];

export const weeksOfMonthOptions = [
  { value: 1, label: '1st week' },
  { value: 2, label: '2nd week' },
  { value: 3, label: '3rd week' },
  { value: 4, label: '4th week' },
  { value: -1, label: 'Last week' }
];

export const daysOfMonthOptions = [1, 5, 10, 15, 20, 25, -1];

export const monthsOfYearOptions = [
  { value: 1, label: 'Jan' },
  { value: 2, label: 'Feb' },
  { value: 3, label: 'Mar' },
  { value: 4, label: 'Apr' },
  { value: 5, label: 'May' },
  { value: 6, label: 'Jun' },
  { value: 7, label: 'Jul' },
  { value: 8, label: 'Aug' },
  { value: 9, label: 'Sep' },
  { value: 10, label: 'Oct' },
  { value: 11, label: 'Nov' },
  { value: 12, label: 'Dec' }
];

export const colorPalette = [
  0x14b8a6,
  0x22c55e,
  0x84cc16,
  0xeab308,
  0xf97316,
  0xef4444,
  0xec4899,
  0xa855f7,
  0x8b5cf6,
  0x6366f1,
  0x3b82f6,
  0x06b6d4
];
