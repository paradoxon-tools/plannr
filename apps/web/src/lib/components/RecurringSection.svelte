<script lang="ts">
  import type { Account, Contract, Currency, Partner, Pocket, RecurringForm, RecurringTransaction } from '$lib/types';
  import { createEventDispatcher } from 'svelte';
  import {
    daysOfMonthOptions,
    daysOfWeekOptions,
    monthsOfYearOptions,
    recurrenceTypes,
    transactionTypes,
    updateModes,
    weeksOfMonthOptions
  } from '$lib/types';
  import { formatMoney, recurrenceSummary, toggleNumber, toggleString } from '$lib/utils';

  export let form: RecurringForm;
  export let transactions: RecurringTransaction[] = [];
  export let pockets: Pocket[] = [];
  export let contracts: Contract[] = [];
  export let partners: Partner[] = [];
  export let currencies: Currency[] = [];
  export let selectedAccount: Account | null = null;
  export let selectedAccountId = '';
  export let showArchived = false;
  export let isSaving = false;

  const dispatch = createEventDispatcher<{ submit: void; reset: void; edit: { item: RecurringTransaction }; archive: { item: RecurringTransaction } }>();

  $: availablePockets = pockets.filter((pocket) => (!selectedAccountId || pocket.accountId === selectedAccountId) && !pocket.isArchived);
  $: availableContracts = contracts.filter((contract) => (!selectedAccountId || contract.accountId === selectedAccountId) && !contract.isArchived);
  $: scopedTransactions = transactions.filter((item) => (!selectedAccountId || item.accountId === selectedAccountId) && (showArchived || !item.isArchived));

  function pocketName(pocketId: string | null) { return pocketId ? pockets.find((item) => item.id === pocketId)?.name ?? pocketId : '—'; }
</script>
<section class="workspace-grid">
  <article class="panel form-panel">
    <div class="section-heading"><div><p class="eyebrow">Recurring transactions</p><h3>{form.id ? 'Edit recurring transaction' : 'Create recurring transaction'}</h3></div>{#if form.id}<button type="button" class="secondary" on:click={() => dispatch('reset')}>New recurring item</button>{/if}</div>
    <form class="form-grid" on:submit|preventDefault={() => dispatch('submit')}>
      {#if form.id}
        <label><span>Update mode</span><select bind:value={form.updateMode}>{#each updateModes as mode}<option value={mode.value}>{mode.label}</option>{/each}</select></label>
        <label><span>Effective from</span><input bind:value={form.effectiveFromDate} type="date" disabled={form.updateMode !== 'effective_from'} /></label>
      {/if}
      <label><span>Linked contract</span><select bind:value={form.contractId}><option value="">Standalone</option>{#each availableContracts as contract}<option value={contract.id}>{contract.name}</option>{/each}</select></label>
      <label><span>Partner</span><select bind:value={form.partnerId}><option value="">No partner</option>{#each partners.filter((partner) => !partner.isArchived) as partner}<option value={partner.id}>{partner.name}</option>{/each}</select></label>
      <label><span>Title</span><input bind:value={form.title} required /></label>
      <label><span>Amount (minor units)</span><input bind:value={form.amount} type="number" min="0" required /></label>
      <label><span>Currency</span><select bind:value={form.currencyCode}>{#each currencies as currency}<option value={currency.code}>{currency.code} · {currency.name}</option>{/each}</select></label>
      <label><span>Type</span><select bind:value={form.transactionType}>{#each transactionTypes as type}<option value={type.value}>{type.label}</option>{/each}</select></label>
      <label><span>Source pocket</span><select bind:value={form.sourcePocketId}><option value="">None</option>{#each availablePockets as pocket}<option value={pocket.id}>{pocket.name}</option>{/each}</select></label>
      <label><span>Destination pocket</span><select bind:value={form.destinationPocketId}><option value="">None</option>{#each availablePockets as pocket}<option value={pocket.id}>{pocket.name}</option>{/each}</select></label>
      <label><span>First occurrence</span><input bind:value={form.firstOccurrenceDate} type="date" required /></label>
      <label><span>Final occurrence</span><input bind:value={form.finalOccurrenceDate} type="date" /></label>
      <label><span>Recurrence</span><select bind:value={form.recurrenceType}>{#each recurrenceTypes as recurrence}<option value={recurrence.value}>{recurrence.label}</option>{/each}</select></label>
      <label><span>Skip count</span><input bind:value={form.skipCount} type="number" min="0" required /></label>
      <label class="full-span"><span>Description</span><textarea bind:value={form.description} rows="3"></textarea></label>

      <div class="full-span"><span class="label">Days of week</span><div class="chip-row">{#each daysOfWeekOptions as option}<button type="button" class:active={form.daysOfWeek.includes(option.value)} class="chip" on:click={() => (form.daysOfWeek = toggleString(form.daysOfWeek, option.value))}>{option.label}</button>{/each}</div></div>
      <div class="full-span"><span class="label">Weeks of month</span><div class="chip-row">{#each weeksOfMonthOptions as option}<button type="button" class:active={form.weeksOfMonth.includes(option.value)} class="chip" on:click={() => (form.weeksOfMonth = toggleNumber(form.weeksOfMonth, option.value))}>{option.label}</button>{/each}</div></div>
      <div class="full-span"><span class="label">Days of month</span><div class="chip-row">{#each daysOfMonthOptions as day}<button type="button" class:active={form.daysOfMonth.includes(day)} class="chip" on:click={() => (form.daysOfMonth = toggleNumber(form.daysOfMonth, day))}>{day === -1 ? 'Last day' : `Day ${day}`}</button>{/each}</div></div>
      <div class="full-span"><span class="label">Months of year</span><div class="chip-row">{#each monthsOfYearOptions as month}<button type="button" class:active={form.monthsOfYear.includes(month.value)} class="chip" on:click={() => (form.monthsOfYear = toggleNumber(form.monthsOfYear, month.value))}>{month.label}</button>{/each}</div></div>
      <div class="form-actions full-span"><button class="primary" type="submit" disabled={isSaving}>{form.id ? 'Save recurring transaction' : 'Create recurring transaction'}</button></div>
    </form>
  </article>
  <article class="panel list-panel">
    <div class="section-heading"><div><p class="eyebrow">Recurring transactions</p><h3>{selectedAccount ? `For ${selectedAccount.name}` : 'All accounts'}</h3></div><label class="toggle-inline"><input bind:checked={showArchived} type="checkbox" /><span>Show archived</span></label></div>
    {#if scopedTransactions.length}<div class="entity-list">{#each scopedTransactions as item}<article class="entity-card"><div class="entity-topline"><div><h4>{item.title}</h4><p>{formatMoney(item.amount, item.currencyCode, currencies)} · {item.transactionType}</p></div><span class:archived={item.isArchived} class="status-pill">{item.isArchived ? 'Archived' : 'Active'}</span></div><div class="meta-row"><span>{pocketName(item.sourcePocketId)} → {pocketName(item.destinationPocketId)}</span><span>{item.firstOccurrenceDate}</span></div><p class="card-text">{recurrenceSummary(item)}</p><div class="entity-actions"><button type="button" class="secondary" on:click={() => dispatch('edit', { item })}>Edit</button><button type="button" class="ghost" on:click={() => dispatch('archive', { item })}>{item.isArchived ? 'Unarchive' : 'Archive'}</button></div></article>{/each}</div>{:else}<div class="empty-state">No recurring transactions in this scope.</div>{/if}
  </article>
</section>
<style>@import './shared.css';</style>
