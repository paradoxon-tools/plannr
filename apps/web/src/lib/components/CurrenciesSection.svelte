<script lang="ts">
  import type { Currency, CurrencyForm } from '$lib/types';
  import { symbolPositions } from '$lib/types';
  import { createEventDispatcher } from 'svelte';

  export let form: CurrencyForm;
  export let currencies: Currency[] = [];
  export let isSaving = false;

  const dispatch = createEventDispatcher<{ submit: void; reset: void; edit: { currency: Currency } }>();
</script>
<section class="workspace-grid">
  <article class="panel form-panel">
    <div class="section-heading"><div><p class="eyebrow">Currencies</p><h3>{form.originalCode ? 'Edit currency' : 'Create currency'}</h3></div>{#if form.originalCode}<button type="button" class="secondary" on:click={() => dispatch('reset')}>New currency</button>{/if}</div>
    <form class="form-grid" on:submit|preventDefault={() => dispatch('submit')}>
      <label><span>Code</span><input bind:value={form.code} maxlength="3" placeholder="EUR" required /></label>
      <label><span>Name</span><input bind:value={form.name} placeholder="Euro" required /></label>
      <label><span>Symbol</span><input bind:value={form.symbol} placeholder="€" required /></label>
      <label><span>Decimal places</span><input bind:value={form.decimalPlaces} type="number" min="0" max="6" required /></label>
      <label><span>Symbol position</span><select bind:value={form.symbolPosition}>{#each symbolPositions as option}<option value={option.value}>{option.label}</option>{/each}</select></label>
      <div class="form-actions full-span"><button class="primary" type="submit" disabled={isSaving}>{form.originalCode ? 'Save currency' : 'Create currency'}</button></div>
    </form>
  </article>
  <article class="panel list-panel">
    <div class="section-heading"><div><p class="eyebrow">Currencies</p><h3>Available currencies</h3></div></div>
    {#if currencies.length}<div class="entity-list">{#each currencies as currency}<article class="entity-card"><div class="entity-topline"><div><h4>{currency.code}</h4><p>{currency.name}</p></div><span class="status-pill">{currency.symbol}</span></div><div class="meta-row"><span>{currency.decimalPlaces} decimals</span><span>{currency.symbolPosition}</span></div><div class="entity-actions"><button type="button" class="secondary" on:click={() => dispatch('edit', { currency })}>Edit</button></div></article>{/each}</div>{:else}<div class="empty-state">No currencies available.</div>{/if}
  </article>
</section>
<style>@import './shared.css';</style>
