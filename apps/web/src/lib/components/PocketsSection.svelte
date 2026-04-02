<script lang="ts">
  import type { Account, Pocket, PocketForm } from '$lib/types';
  import { colorPalette } from '$lib/types';
  import { createEventDispatcher } from 'svelte';
  import { accountDisplayName, hexColor } from '$lib/utils';

  export let form: PocketForm;
  export let accounts: Account[] = [];
  export let pockets: Pocket[] = [];
  export let selectedAccount: Account | null = null;
  export let selectedAccountId = '';
  export let showArchived = false;
  export let isSaving = false;

  const dispatch = createEventDispatcher<{
    submit: void;
    reset: void;
    edit: { pocket: Pocket };
    archive: { pocket: Pocket };
    addContract: { pocket: Pocket };
  }>();

  $: scopedPockets = pockets.filter((pocket) => (!selectedAccountId || pocket.accountId === selectedAccountId) && (showArchived || !pocket.isArchived));
</script>

<section class="workspace-grid">
  <article class="panel form-panel">
    <div class="section-heading">
      <div><p class="eyebrow">Pockets</p><h3>{form.id ? 'Edit pocket' : 'Create pocket'}</h3></div>
      {#if form.id}<button type="button" class="secondary" on:click={() => dispatch('reset')}>New pocket</button>{/if}
    </div>
    <form class="form-grid" on:submit|preventDefault={() => dispatch('submit')}>
      <label><span>Account</span><select bind:value={form.accountId} required><option value="" disabled>Select account</option>{#each accounts.filter((account) => !account.isArchived) as account}<option value={account.id}>{account.name} · {account.institution}</option>{/each}</select></label>
      <label><span>Name</span><input bind:value={form.name} placeholder="Bills" required /></label>
      <label class="full-span"><span>Description</span><textarea bind:value={form.description} rows="3" placeholder="What lives inside this pocket?"></textarea></label>
      <div class="full-span"><span class="label">Color</span><div class="color-palette">{#each colorPalette as color}<button type="button" aria-label={`Select ${hexColor(color)}`} class:active={form.color === color} class="color-chip" style={`background:${hexColor(color)}`} on:click={() => (form.color = color)}></button>{/each}</div></div>
      <label class="checkbox-field full-span"><input bind:checked={form.isDefault} type="checkbox" /><span>Default pocket for this account</span></label>
      <div class="form-actions full-span"><button class="primary" type="submit" disabled={isSaving || !form.accountId}>{form.id ? 'Save pocket' : 'Create pocket'}</button></div>
    </form>
  </article>
  <article class="panel list-panel">
    <div class="section-heading">
      <div><p class="eyebrow">Scoped list</p><h3>Pockets {selectedAccount ? `for ${selectedAccount.name}` : ''}</h3></div>
      <label class="toggle-inline"><input bind:checked={showArchived} type="checkbox" /><span>Show archived</span></label>
    </div>
    {#if scopedPockets.length}
      <div class="entity-list">
        {#each scopedPockets as pocket}
          <article class="entity-card">
            <div class="entity-topline">
              <div class="entity-title-with-color"><span class="swatch" style={`background:${hexColor(pocket.color)}`}></span><div><h4>{pocket.name}</h4><p>{accountDisplayName(pocket.accountId, accounts)}</p></div></div>
              <span class:archived={pocket.isArchived} class="status-pill">{pocket.isArchived ? 'Archived' : pocket.isDefault ? 'Default' : 'Active'}</span>
            </div>
            <p class="card-text">{pocket.description || 'No description'}</p>
            <div class="entity-actions">
              <button type="button" class="secondary" on:click={() => dispatch('edit', { pocket })}>Edit</button>
              <button type="button" class="ghost" on:click={() => dispatch('addContract', { pocket })}>Add contract</button>
              <button type="button" class="ghost" on:click={() => dispatch('archive', { pocket })}>{pocket.isArchived ? 'Unarchive' : 'Archive'}</button>
            </div>
          </article>
        {/each}
      </div>
    {:else}<div class="empty-state">No pockets for the current scope yet.</div>{/if}
  </article>
</section>

<style>@import './shared.css';</style>
