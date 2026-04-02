<script lang="ts">
  import type { Partner, PartnerForm } from '$lib/types';
  import { createEventDispatcher } from 'svelte';
  import { formatCreatedAt } from '$lib/utils';

  export let form: PartnerForm;
  export let partners: Partner[] = [];
  export let showArchived = false;
  export let isSaving = false;

  const dispatch = createEventDispatcher<{ submit: void; reset: void; edit: { partner: Partner }; archive: { partner: Partner } }>();
  $: visiblePartners = partners.filter((partner) => showArchived || !partner.isArchived);
</script>
<section class="workspace-grid">
  <article class="panel form-panel">
    <div class="section-heading"><div><p class="eyebrow">Partners</p><h3>{form.id ? 'Edit partner' : 'Create partner'}</h3></div>{#if form.id}<button type="button" class="secondary" on:click={() => dispatch('reset')}>New partner</button>{/if}</div>
    <form class="form-grid" on:submit|preventDefault={() => dispatch('submit')}>
      <label><span>Name</span><input bind:value={form.name} placeholder="Landlord / Employer / Gym" required /></label>
      <label class="full-span"><span>Notes</span><textarea bind:value={form.notes} rows="4"></textarea></label>
      <div class="form-actions full-span"><button class="primary" type="submit" disabled={isSaving}>{form.id ? 'Save partner' : 'Create partner'}</button></div>
    </form>
  </article>
  <article class="panel list-panel">
    <div class="section-heading"><div><p class="eyebrow">Partners</p><h3>All partners</h3></div><label class="toggle-inline"><input bind:checked={showArchived} type="checkbox" /><span>Show archived</span></label></div>
    {#if visiblePartners.length}<div class="entity-list">{#each visiblePartners as partner}<article class="entity-card"><div class="entity-topline"><div><h4>{partner.name}</h4><p>{formatCreatedAt(partner.createdAt)}</p></div><span class:archived={partner.isArchived} class="status-pill">{partner.isArchived ? 'Archived' : 'Active'}</span></div><p class="card-text">{partner.notes || 'No notes'}</p><div class="entity-actions"><button type="button" class="secondary" on:click={() => dispatch('edit', { partner })}>Edit</button><button type="button" class="ghost" on:click={() => dispatch('archive', { partner })}>{partner.isArchived ? 'Unarchive' : 'Archive'}</button></div></article>{/each}</div>{:else}<div class="empty-state">No partners yet.</div>{/if}
  </article>
</section>
<style>@import './shared.css';</style>
