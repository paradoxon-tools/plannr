<script lang="ts">
  import type { Account, AccountForm, Currency } from '$lib/types';
  import { weekendHandlingOptions } from '$lib/types';
  import { createEventDispatcher } from 'svelte';
  import { formatCreatedAt } from '$lib/utils';

  export let form: AccountForm;
  export let accounts: Account[] = [];
  export let currencies: Currency[] = [];
  export let accountFilter = 'all';
  export let isSaving = false;

  const dispatch = createEventDispatcher<{
    submit: void;
    reset: void;
    edit: { account: Account };
    archive: { account: Account };
    openPockets: { account: Account };
  }>();

  $: visibleAccounts = accounts.filter((account) => {
    if (accountFilter === 'active') return !account.isArchived;
    if (accountFilter === 'archived') return account.isArchived;
    return true;
  });
</script>

<section class="workspace-grid">
  <article class="panel form-panel">
    <div class="section-heading">
      <div><p class="eyebrow">Accounts</p><h3>{form.id ? 'Edit account' : 'Create account'}</h3></div>
      {#if form.id}<button type="button" class="secondary" on:click={() => dispatch('reset')}>New account</button>{/if}
    </div>
    <form class="form-grid" on:submit|preventDefault={() => dispatch('submit')}>
      <label><span>Name</span><input bind:value={form.name} placeholder="Primary checking" required /></label>
      <label><span>Institution</span><input bind:value={form.institution} placeholder="ING / N26 / Local bank" required /></label>
      <label><span>Currency</span><select bind:value={form.currencyCode}>{#each currencies as currency}<option value={currency.code}>{currency.code} · {currency.name}</option>{/each}</select></label>
      <label><span>Weekend handling</span><select bind:value={form.weekendHandling}>{#each weekendHandlingOptions as option}<option value={option.value}>{option.label}</option>{/each}</select></label>
      <div class="form-actions full-span"><button class="primary" type="submit" disabled={isSaving}>{form.id ? 'Save account' : 'Create account'}</button></div>
    </form>
  </article>
  <article class="panel list-panel">
    <div class="section-heading">
      <div><p class="eyebrow">Directory</p><h3>Accounts</h3></div>
      <div class="segmented">
        <button type="button" class:active={accountFilter === 'all'} on:click={() => (accountFilter = 'all')}>All</button>
        <button type="button" class:active={accountFilter === 'active'} on:click={() => (accountFilter = 'active')}>Active</button>
        <button type="button" class:active={accountFilter === 'archived'} on:click={() => (accountFilter = 'archived')}>Archived</button>
      </div>
    </div>
    {#if visibleAccounts.length}
      <div class="entity-list">
        {#each visibleAccounts as account}
          <article class="entity-card">
            <div class="entity-topline">
              <div><h4>{account.name}</h4><p>{account.institution}</p></div>
              <span class:archived={account.isArchived} class="status-pill">{account.isArchived ? 'Archived' : 'Active'}</span>
            </div>
            <div class="meta-row">
              <span>{account.currencyCode}</span>
              <span>{account.weekendHandling.replaceAll('_', ' ')}</span>
              <span>{formatCreatedAt(account.createdAt)}</span>
            </div>
            <div class="entity-actions">
              <button type="button" class="secondary" on:click={() => dispatch('edit', { account })}>Edit</button>
              <button type="button" class="ghost" on:click={() => dispatch('openPockets', { account })}>Open pockets</button>
              <button type="button" class="ghost" on:click={() => dispatch('archive', { account })}>{account.isArchived ? 'Unarchive' : 'Archive'}</button>
            </div>
          </article>
        {/each}
      </div>
    {:else}<div class="empty-state">No accounts yet.</div>{/if}
  </article>
</section>

<style>
  @import './shared.css';
</style>
