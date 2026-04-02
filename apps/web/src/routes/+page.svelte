<script lang="ts">
  import { onMount } from 'svelte';
  import Sidebar from '$lib/components/Sidebar.svelte';
  import NoticeBanner from '$lib/components/NoticeBanner.svelte';
  import OverviewSection from '$lib/components/OverviewSection.svelte';
  import AccountsSection from '$lib/components/AccountsSection.svelte';
  import PocketsSection from '$lib/components/PocketsSection.svelte';
  import ContractsSection from '$lib/components/ContractsSection.svelte';
  import RecurringSection from '$lib/components/RecurringSection.svelte';
  import PartnersSection from '$lib/components/PartnersSection.svelte';
  import CurrenciesSection from '$lib/components/CurrenciesSection.svelte';
  import type {
    Account,
    AccountForm,
    ApiErrorEnvelope,
    Contract,
    ContractForm,
    Currency,
    CurrencyForm,
    Notice,
    Partner,
    PartnerForm,
    Pocket,
    PocketForm,
    RecurringForm,
    RecurringTransaction,
    SectionId
  } from '$lib/types';
  import {
    createEmptyAccountForm,
    createEmptyContractForm,
    createEmptyCurrencyForm,
    createEmptyPartnerForm,
    createEmptyPocketForm,
    createEmptyRecurringForm,
    dedupeBy,
    getErrorMessage,
    normalizeOptionalString,
    normalizedArray,
    sortByCreatedAt
  } from '$lib/utils';
  import { colorPalette } from '$lib/types';

  export let data: { apiBaseUrl: string };

  let isLoading = true;
  let isSaving = false;
  let activeSection: SectionId = 'overview';
  let selectedAccountId = '';
  let accountFilter = 'all';
  let showArchivedPockets = false;
  let showArchivedContracts = false;
  let showArchivedRecurring = false;
  let showArchivedPartners = false;
  let notice: Notice = null;
  let lastSyncedAt = '';

  let accounts: Account[] = [];
  let pockets: Pocket[] = [];
  let partners: Partner[] = [];
  let contracts: Contract[] = [];
  let recurringTransactions: RecurringTransaction[] = [];
  let currencies: Currency[] = [];

  let accountForm: AccountForm = createEmptyAccountForm();
  let pocketForm: PocketForm = createEmptyPocketForm();
  let partnerForm: PartnerForm = createEmptyPartnerForm();
  let contractForm: ContractForm = createEmptyContractForm();
  let recurringForm: RecurringForm = createEmptyRecurringForm();
  let currencyForm: CurrencyForm = createEmptyCurrencyForm();

  async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${data.apiBaseUrl}${path}`, {
      headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
      ...init
    });

    if (!response.ok) {
      let message = `Request failed with status ${response.status}`;
      try {
        const payload = (await response.json()) as ApiErrorEnvelope;
        message = payload.error?.message ?? message;
      } catch {
        // ignore
      }
      throw new Error(message);
    }

    if (response.status === 204) return undefined as T;
    return (await response.json()) as T;
  }

  function setNotice(tone: NonNullable<Notice>['tone'], message: string) {
    notice = { tone, message };
  }

  async function loadAll() {
    isLoading = true;
    try {
      const [loadedAccounts, loadedCurrencies, activePockets, archivedPockets, activePartners, archivedPartners, activeContracts, archivedContracts, activeRecurring, archivedRecurring] =
        await Promise.all([
          api<Account[]>('/accounts'),
          api<Currency[]>('/currencies'),
          api<Pocket[]>('/pockets?archived=false'),
          api<Pocket[]>('/pockets?archived=true'),
          api<Partner[]>('/partners?archived=false'),
          api<Partner[]>('/partners?archived=true'),
          api<Contract[]>('/contracts?archived=false'),
          api<Contract[]>('/contracts?archived=true'),
          api<RecurringTransaction[]>('/recurring-transactions?archived=false'),
          api<RecurringTransaction[]>('/recurring-transactions?archived=true')
        ]);

      accounts = sortByCreatedAt(loadedAccounts);
      currencies = [...loadedCurrencies].sort((a, b) => a.code.localeCompare(b.code));
      pockets = sortByCreatedAt(dedupeBy([...activePockets, ...archivedPockets], (item) => item.id));
      partners = sortByCreatedAt(dedupeBy([...activePartners, ...archivedPartners], (item) => item.id));
      contracts = sortByCreatedAt(dedupeBy([...activeContracts, ...archivedContracts], (item) => item.id));
      recurringTransactions = sortByCreatedAt(dedupeBy([...activeRecurring, ...archivedRecurring], (item) => item.id));

      if (!selectedAccountId || !accounts.some((item) => item.id === selectedAccountId)) {
        selectedAccountId = accounts.find((item) => !item.isArchived)?.id ?? accounts[0]?.id ?? '';
      }

      syncDefaultFormValues();
      lastSyncedAt = new Date().toLocaleTimeString();
    } finally {
      isLoading = false;
    }
  }

  function syncDefaultFormValues() {
    const defaultCurrency = selectedAccount?.currencyCode ?? currencies[0]?.code ?? 'EUR';
    if (!accountForm.currencyCode) accountForm.currencyCode = defaultCurrency;
    if (!pocketForm.accountId) pocketForm.accountId = selectedAccountId;
    if (!contractForm.accountId) contractForm.accountId = selectedAccountId;
    if (!recurringForm.currencyCode) recurringForm.currencyCode = defaultCurrency;
    if (!currencyForm.code) currencyForm.code = currencies[0]?.code ?? 'EUR';
  }

  onMount(async () => {
    try {
      await loadAll();
      setNotice('info', 'Connected to plannr API. Start by creating an account, then add pockets.');
    } catch (error) {
      setNotice('error', getErrorMessage(error));
    }
  });

  $: selectedAccount = accounts.find((item) => item.id === selectedAccountId) ?? null;
  $: activeAccountCount = accounts.filter((item) => !item.isArchived).length;
  $: activePocketCount = pockets.filter((item) => !item.isArchived).length;
  $: activeContractCount = contracts.filter((item) => !item.isArchived).length;
  $: activeRecurringCount = recurringTransactions.filter((item) => !item.isArchived).length;

  function resetAccountForm() { accountForm = createEmptyAccountForm(selectedAccount?.currencyCode ?? currencies[0]?.code ?? 'EUR'); }
  function resetPocketForm() { pocketForm = createEmptyPocketForm(selectedAccountId); }
  function resetPartnerForm() { partnerForm = createEmptyPartnerForm(); }
  function resetContractForm() { contractForm = createEmptyContractForm(selectedAccountId); }
  function resetRecurringForm() { recurringForm = createEmptyRecurringForm(selectedAccount?.currencyCode ?? currencies[0]?.code ?? 'EUR'); }
  function resetCurrencyForm() { currencyForm = createEmptyCurrencyForm(currencies[0]?.code ?? 'EUR'); }

  function editAccount(account: Account) {
    activeSection = 'accounts';
    selectedAccountId = account.id;
    accountForm = { id: account.id, name: account.name, institution: account.institution, currencyCode: account.currencyCode, weekendHandling: account.weekendHandling };
  }

  function editPocket(pocket: Pocket) {
    activeSection = 'pockets';
    selectedAccountId = pocket.accountId;
    pocketForm = { id: pocket.id, accountId: pocket.accountId, name: pocket.name, description: pocket.description ?? '', color: pocket.color, isDefault: pocket.isDefault };
  }

  function editPartner(partner: Partner) {
    activeSection = 'partners';
    partnerForm = { id: partner.id, name: partner.name, notes: partner.notes ?? '' };
  }

  function editContract(contract: Contract) {
    activeSection = 'contracts';
    selectedAccountId = contract.accountId;
    contractForm = { id: contract.id, accountId: contract.accountId, pocketId: contract.pocketId, partnerId: contract.partnerId ?? '', name: contract.name, startDate: contract.startDate, endDate: contract.endDate ?? '', notes: contract.notes ?? '' };
  }

  function editRecurring(item: RecurringTransaction) {
    activeSection = 'recurring';
    selectedAccountId = item.accountId;
    recurringForm = {
      id: item.id,
      updateMode: 'overwrite',
      effectiveFromDate: '',
      contractId: item.contractId ?? '',
      sourcePocketId: item.sourcePocketId ?? '',
      destinationPocketId: item.destinationPocketId ?? '',
      partnerId: item.partnerId ?? '',
      title: item.title,
      description: item.description ?? '',
      amount: item.amount,
      currencyCode: item.currencyCode,
      transactionType: item.transactionType,
      firstOccurrenceDate: item.firstOccurrenceDate,
      finalOccurrenceDate: item.finalOccurrenceDate ?? '',
      recurrenceType: item.recurrenceType,
      skipCount: item.skipCount,
      daysOfWeek: item.daysOfWeek ?? [],
      weeksOfMonth: item.weeksOfMonth ?? [],
      daysOfMonth: item.daysOfMonth ?? [],
      monthsOfYear: item.monthsOfYear ?? []
    };
  }

  function editCurrency(currency: Currency) {
    activeSection = 'currencies';
    currencyForm = { code: currency.code, originalCode: currency.code, name: currency.name, symbol: currency.symbol, decimalPlaces: currency.decimalPlaces, symbolPosition: currency.symbolPosition };
  }

  async function submitAccount() {
    isSaving = true;
    try {
      const payload = { name: accountForm.name, institution: accountForm.institution, currencyCode: accountForm.currencyCode, weekendHandling: accountForm.weekendHandling };
      if (accountForm.id) await api(`/accounts/${accountForm.id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api('/accounts', { method: 'POST', body: JSON.stringify(payload) });
      await loadAll();
      resetAccountForm();
      activeSection = 'pockets';
      setNotice('success', 'Account saved.');
    } catch (error) { setNotice('error', getErrorMessage(error)); } finally { isSaving = false; }
  }

  async function submitPocket() {
    isSaving = true;
    try {
      const payload = { accountId: pocketForm.accountId, name: pocketForm.name, description: normalizeOptionalString(pocketForm.description), color: pocketForm.color, isDefault: pocketForm.isDefault };
      if (pocketForm.id) await api(`/pockets/${pocketForm.id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api('/pockets', { method: 'POST', body: JSON.stringify(payload) });
      await loadAll();
      resetPocketForm();
      setNotice('success', 'Pocket saved.');
    } catch (error) { setNotice('error', getErrorMessage(error)); } finally { isSaving = false; }
  }

  async function submitPartner() {
    isSaving = true;
    try {
      const payload = { name: partnerForm.name, notes: normalizeOptionalString(partnerForm.notes) };
      if (partnerForm.id) await api(`/partners/${partnerForm.id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api('/partners', { method: 'POST', body: JSON.stringify(payload) });
      await loadAll();
      resetPartnerForm();
      setNotice('success', 'Partner saved.');
    } catch (error) { setNotice('error', getErrorMessage(error)); } finally { isSaving = false; }
  }

  async function submitContract() {
    isSaving = true;
    try {
      let pocketId = contractForm.pocketId;
      const existingContract = contractForm.id ? contracts.find((item) => item.id === contractForm.id) : null;
      const existingPocket = pocketId ? pockets.find((item) => item.id === pocketId) : null;
      const needsNewPocket = !contractForm.id || existingContract?.accountId !== contractForm.accountId || !pocketId;

      if (needsNewPocket) {
        const createdPocket = await api<Pocket>('/pockets', {
          method: 'POST',
          body: JSON.stringify({
            accountId: contractForm.accountId,
            name: contractForm.name,
            description: normalizeOptionalString(contractForm.notes),
            color: colorPalette[8],
            isDefault: false
          })
        });
        pocketId = createdPocket.id;
      } else if (existingPocket) {
        await api<Pocket>(`/pockets/${existingPocket.id}`, {
          method: 'PUT',
          body: JSON.stringify({
            accountId: existingPocket.accountId,
            name: contractForm.name,
            description: normalizeOptionalString(contractForm.notes),
            color: existingPocket.color,
            isDefault: existingPocket.isDefault
          })
        });
      }

      const payload = {
        pocketId,
        partnerId: normalizeOptionalString(contractForm.partnerId),
        name: contractForm.name,
        startDate: contractForm.startDate,
        endDate: normalizeOptionalString(contractForm.endDate),
        notes: normalizeOptionalString(contractForm.notes)
      };
      if (contractForm.id) await api(`/contracts/${contractForm.id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api('/contracts', { method: 'POST', body: JSON.stringify(payload) });
      await loadAll();
      resetContractForm();
      setNotice('success', 'Contract saved and pocket prepared automatically.');
    } catch (error) { setNotice('error', getErrorMessage(error)); } finally { isSaving = false; }
  }

  async function submitRecurring() {
    isSaving = true;
    try {
      const payload = {
        ...(recurringForm.id ? { updateMode: recurringForm.updateMode, effectiveFromDate: recurringForm.updateMode === 'effective_from' ? normalizeOptionalString(recurringForm.effectiveFromDate) : null } : {}),
        contractId: normalizeOptionalString(recurringForm.contractId),
        sourcePocketId: normalizeOptionalString(recurringForm.sourcePocketId),
        destinationPocketId: normalizeOptionalString(recurringForm.destinationPocketId),
        partnerId: normalizeOptionalString(recurringForm.partnerId),
        title: recurringForm.title,
        description: normalizeOptionalString(recurringForm.description),
        amount: Number(recurringForm.amount),
        currencyCode: recurringForm.currencyCode,
        transactionType: recurringForm.transactionType,
        firstOccurrenceDate: recurringForm.firstOccurrenceDate,
        finalOccurrenceDate: normalizeOptionalString(recurringForm.finalOccurrenceDate),
        recurrenceType: recurringForm.recurrenceType,
        skipCount: Number(recurringForm.skipCount),
        daysOfWeek: ['daily', 'weekly', 'monthly'].includes(recurringForm.recurrenceType) ? normalizedArray(recurringForm.daysOfWeek) : null,
        weeksOfMonth: ['weekly', 'monthly'].includes(recurringForm.recurrenceType) ? normalizedArray(recurringForm.weeksOfMonth) : null,
        daysOfMonth: recurringForm.recurrenceType === 'monthly' ? normalizedArray(recurringForm.daysOfMonth) : null,
        monthsOfYear: recurringForm.recurrenceType === 'monthly' ? normalizedArray(recurringForm.monthsOfYear) : null
      };
      if (recurringForm.id) await api(`/recurring-transactions/${recurringForm.id}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api('/recurring-transactions', { method: 'POST', body: JSON.stringify(payload) });
      await loadAll();
      resetRecurringForm();
      setNotice('success', 'Recurring transaction saved.');
    } catch (error) { setNotice('error', getErrorMessage(error)); } finally { isSaving = false; }
  }

  async function submitCurrency() {
    isSaving = true;
    try {
      const payload = { code: currencyForm.code, name: currencyForm.name, symbol: currencyForm.symbol, decimalPlaces: Number(currencyForm.decimalPlaces), symbolPosition: currencyForm.symbolPosition };
      if (currencyForm.originalCode) await api(`/currencies/${currencyForm.originalCode}`, { method: 'PUT', body: JSON.stringify(payload) });
      else await api('/currencies', { method: 'POST', body: JSON.stringify(payload) });
      await loadAll();
      resetCurrencyForm();
      setNotice('success', 'Currency saved.');
    } catch (error) { setNotice('error', getErrorMessage(error)); } finally { isSaving = false; }
  }

  async function toggleArchive(resource: 'accounts' | 'pockets' | 'partners' | 'contracts' | 'recurring-transactions', id: string, archived: boolean) {
    if (!window.confirm(`${archived ? 'Unarchive' : 'Archive'} this item?`)) return;
    isSaving = true;
    try {
      await api(`/${resource}/${id}/${archived ? 'unarchive' : 'archive'}`, { method: 'POST', body: JSON.stringify({}) });
      await loadAll();
      setNotice('success', archived ? 'Item restored.' : 'Item archived.');
    } catch (error) { setNotice('error', getErrorMessage(error)); } finally { isSaving = false; }
  }
</script>

<svelte:head>
  <title>plannr studio</title>
  <meta name="description" content="Beautiful plannr control center for accounts, pockets, contracts, recurring transactions, partners and currencies." />
</svelte:head>

<div class="page-shell">
  <div class="bg-orb orb-a"></div>
  <div class="bg-orb orb-b"></div>
  <div class="bg-grid"></div>

  <main class="layout">
    <Sidebar bind:activeSection bind:selectedAccountId {accounts} {activeAccountCount} {activePocketCount} {activeContractCount} {activeRecurringCount} />

    <section class="content">
      <div class="toolbar">
        <div class="toolbar-meta">
          <p class="eyebrow">plannr</p>
          <p class="toolbar-copy">API: <code>{data.apiBaseUrl}</code></p>
        </div>
        <div class="hero-actions">
          <button type="button" class="primary" on:click={loadAll} disabled={isLoading || isSaving}>{isLoading ? 'Refreshing…' : 'Refresh data'}</button>
          <button type="button" class="secondary" on:click={() => (activeSection = 'accounts')}>Start with account</button>
        </div>
      </div>

      <NoticeBanner {notice} on:close={() => (notice = null)} />

      {#if activeSection === 'overview'}
        <OverviewSection selectedAccount={selectedAccount} lastSyncedAt={lastSyncedAt} accountsCount={accounts.length} pocketsCount={pockets.length} contractsCount={contracts.length} recurringCount={recurringTransactions.length} openAccounts={() => (activeSection = 'accounts')} openPockets={() => (activeSection = 'pockets')} />
      {/if}

      {#if activeSection === 'accounts'}
        <AccountsSection bind:form={accountForm} bind:accountFilter {accounts} {currencies} {isSaving}
          on:submit={submitAccount}
          on:reset={resetAccountForm}
          on:edit={(e) => editAccount(e.detail.account)}
          on:archive={(e) => toggleArchive('accounts', e.detail.account.id, e.detail.account.isArchived)}
          on:openPockets={(e) => { selectedAccountId = e.detail.account.id; activeSection = 'pockets'; }} />
      {/if}

      {#if activeSection === 'pockets'}
        <PocketsSection bind:form={pocketForm} bind:showArchived={showArchivedPockets} {accounts} pockets={pockets} {selectedAccount} bind:selectedAccountId {isSaving}
          on:submit={submitPocket}
          on:reset={resetPocketForm}
          on:edit={(e) => editPocket(e.detail.pocket)}
          on:archive={(e) => toggleArchive('pockets', e.detail.pocket.id, e.detail.pocket.isArchived)}
          on:addContract={(e) => { contractForm = { ...createEmptyContractForm(e.detail.pocket.accountId), accountId: e.detail.pocket.accountId }; activeSection = 'contracts'; }} />
      {/if}

      {#if activeSection === 'contracts'}
        <ContractsSection bind:form={contractForm} bind:showArchived={showArchivedContracts} {contracts} pockets={pockets} {partners} {accounts} {selectedAccount} bind:selectedAccountId {isSaving}
          on:submit={submitContract}
          on:reset={resetContractForm}
          on:edit={(e) => editContract(e.detail.contract)}
          on:archive={(e) => toggleArchive('contracts', e.detail.contract.id, e.detail.contract.isArchived)}
          on:addRecurring={(e) => { recurringForm.contractId = e.detail.contract.id; activeSection = 'recurring'; }} />
      {/if}

      {#if activeSection === 'recurring'}
        <RecurringSection bind:form={recurringForm} bind:showArchived={showArchivedRecurring} transactions={recurringTransactions} pockets={pockets} {contracts} {partners} {currencies} {selectedAccount} bind:selectedAccountId {isSaving}
          on:submit={submitRecurring}
          on:reset={resetRecurringForm}
          on:edit={(e) => editRecurring(e.detail.item)}
          on:archive={(e) => toggleArchive('recurring-transactions', e.detail.item.id, e.detail.item.isArchived)} />
      {/if}

      {#if activeSection === 'partners'}
        <PartnersSection bind:form={partnerForm} bind:showArchived={showArchivedPartners} {partners} {isSaving}
          on:submit={submitPartner}
          on:reset={resetPartnerForm}
          on:edit={(e) => editPartner(e.detail.partner)}
          on:archive={(e) => toggleArchive('partners', e.detail.partner.id, e.detail.partner.isArchived)} />
      {/if}

      {#if activeSection === 'currencies'}
        <CurrenciesSection bind:form={currencyForm} {currencies} {isSaving}
          on:submit={submitCurrency}
          on:reset={resetCurrencyForm}
          on:edit={(e) => editCurrency(e.detail.currency)} />
      {/if}
    </section>
  </main>
</div>

<style>
  :global(body) {
    margin: 0;
    min-height: 100vh;
    font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
    color: #e5eefb;
    background: radial-gradient(circle at top, rgba(59, 130, 246, 0.18), transparent 30%), linear-gradient(180deg, #07111f 0%, #0b1220 38%, #0f172a 100%);
  }
  :global(*) { box-sizing: border-box; }
  .page-shell { position: relative; min-height: 100vh; overflow: hidden; }
  .bg-orb { position: fixed; border-radius: 999px; filter: blur(90px); opacity: 0.45; pointer-events: none; }
  .orb-a { width: 24rem; height: 24rem; top: -8rem; right: -6rem; background: rgba(20, 184, 166, 0.32); }
  .orb-b { width: 26rem; height: 26rem; left: -10rem; bottom: -10rem; background: rgba(168, 85, 247, 0.24); }
  .bg-grid { position: fixed; inset: 0; background-image: linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px); background-size: 36px 36px; mask-image: radial-gradient(circle at center, black, transparent 85%); pointer-events: none; }
  .layout { position: relative; z-index: 1; display: grid; grid-template-columns: 21rem minmax(0, 1fr); gap: 1.5rem; padding: 1.5rem; }
  .content { display: grid; gap: 1.5rem; align-content: start; }
  .toolbar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(148, 163, 184, 0.18); }
  .toolbar-meta { display: grid; gap: 0.25rem; }
  .eyebrow { margin: 0 0 .35rem; text-transform: uppercase; letter-spacing: .16em; font-size: .72rem; color: #7dd3fc; }
  p { margin: 0; }
  .toolbar-copy { color:#b6c6dc; line-height:1.6; }
  code { padding: 0.15rem 0.45rem; border-radius: 0.4rem; background: rgba(148, 163, 184, 0.14); }
  .hero-actions { display:flex; gap:.75rem; flex-wrap:wrap; }
  button { font: inherit; }
  .primary,.secondary { border-radius: .95rem; cursor: pointer; border: 1px solid transparent; }
  .primary { padding: .85rem 1.15rem; background: linear-gradient(135deg, #38bdf8, #8b5cf6); color: white; font-weight: 700; }
  .secondary { padding: .78rem 1.05rem; background: rgba(148, 163, 184, 0.12); border-color: rgba(148, 163, 184, 0.16); color: #edf5ff; font-weight: 600; }
  @media (max-width: 1180px) { .layout { grid-template-columns: 1fr; } }
  @media (max-width: 860px) { .toolbar { align-items: start; flex-direction: column; } }
  @media (max-width: 640px) { .layout { padding: 1rem; gap: 1rem; } }
</style>
