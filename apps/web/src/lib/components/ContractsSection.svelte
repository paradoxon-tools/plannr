<script lang="ts">
  import type { Contract, ContractForm, Partner, Pocket, Account } from '$lib/types';
  import { createEventDispatcher } from 'svelte';

  export let form: ContractForm;
  export let contracts: Contract[] = [];
  export let pockets: Pocket[] = [];
  export let partners: Partner[] = [];
  export let accounts: Account[] = [];
  export let selectedAccount: Account | null = null;
  export let selectedAccountId = '';
  export let showArchived = false;
  export let isSaving = false;

  const dispatch = createEventDispatcher<{ submit: void; reset: void; edit: { contract: Contract }; archive: { contract: Contract }; addRecurring: { contract: Contract } }>();

  $: availableAccounts = accounts.filter((account) => !account.isArchived);
  $: scopedContracts = contracts.filter((contract) => (!selectedAccountId || contract.accountId === selectedAccountId) && (showArchived || !contract.isArchived));

  function pocketName(pocketId: string) { return pockets.find((item) => item.id === pocketId)?.name ?? pocketId; }
  function partnerName(partnerId: string | null) { return partnerId ? partners.find((item) => item.id === partnerId)?.name ?? partnerId : 'No partner'; }
</script>
<section class="workspace-grid">
  <article class="panel form-panel">
    <div class="section-heading"><div><p class="eyebrow">Contracts</p><h3>{form.id ? 'Edit contract' : 'Create contract'}</h3></div>{#if form.id}<button type="button" class="secondary" on:click={() => dispatch('reset')}>New contract</button>{/if}</div>
    <form class="form-grid" on:submit|preventDefault={() => dispatch('submit')}>
      <label><span>Account</span><select bind:value={form.accountId} required><option value="" disabled>Select account</option>{#each availableAccounts as account}<option value={account.id}>{account.name} · {account.institution}</option>{/each}</select></label>
      <label><span>Partner</span><select bind:value={form.partnerId}><option value="">No partner</option>{#each partners.filter((partner) => !partner.isArchived) as partner}<option value={partner.id}>{partner.name}</option>{/each}</select></label>
      <label><span>Name</span><input bind:value={form.name} placeholder="Gym membership" required /></label>
      <p class="card-text full-span">A dedicated pocket will be created automatically inside the selected account using the contract name.</p>
      <label><span>Start date</span><input bind:value={form.startDate} type="date" required /></label>
      <label><span>End date</span><input bind:value={form.endDate} type="date" /></label>
      <label class="full-span"><span>Notes</span><textarea bind:value={form.notes} rows="3"></textarea></label>
      <div class="form-actions full-span"><button class="primary" type="submit" disabled={isSaving || !form.accountId}>{form.id ? 'Save contract' : 'Create contract'}</button></div>
    </form>
  </article>
  <article class="panel list-panel">
    <div class="section-heading"><div><p class="eyebrow">Contracts</p><h3>{selectedAccount ? `For ${selectedAccount.name}` : 'All accounts'}</h3></div><label class="toggle-inline"><input bind:checked={showArchived} type="checkbox" /><span>Show archived</span></label></div>
    {#if scopedContracts.length}<div class="entity-list">{#each scopedContracts as contract}<article class="entity-card"><div class="entity-topline"><div><h4>{contract.name}</h4><p>{pocketName(contract.pocketId)} · {partnerName(contract.partnerId)}</p></div><span class:archived={contract.isArchived} class="status-pill">{contract.isArchived ? 'Archived' : 'Active'}</span></div><div class="meta-row"><span>{contract.startDate}</span><span>{contract.endDate || 'Open end'}</span></div><p class="card-text">{contract.notes || 'No notes'}</p><div class="entity-actions"><button type="button" class="secondary" on:click={() => dispatch('edit', { contract })}>Edit</button><button type="button" class="ghost" on:click={() => dispatch('addRecurring', { contract })}>Add recurring</button><button type="button" class="ghost" on:click={() => dispatch('archive', { contract })}>{contract.isArchived ? 'Unarchive' : 'Archive'}</button></div></article>{/each}</div>{:else}<div class="empty-state">No contracts in the current scope.</div>{/if}
  </article>
</section>
<style>@import './shared.css';</style>
