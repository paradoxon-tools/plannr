<script lang="ts">
  import type { Account, SectionId } from '$lib/types';
  import { sections } from '$lib/types';
  export let activeSection: SectionId;
  export let selectedAccountId = '';
  export let accounts: Account[] = [];
  export let activeAccountCount = 0;
  export let activePocketCount = 0;
  export let activeContractCount = 0;
  export let activeRecurringCount = 0;
</script>

<aside class="sidebar panel">
  <div class="brand">
    <div class="brand-mark">P</div>
    <div>
      <p class="eyebrow">plannr studio</p>
      <h1>REST admin</h1>
    </div>
  </div>

  <p class="muted">
    Design-forward control center for every API entity, optimized for the real-world flow: create an
    account, then organize pockets inside it.
  </p>

  <nav class="nav">
    {#each sections as section}
      <button class:active={activeSection === section.id} type="button" on:click={() => (activeSection = section.id)}>
        {section.label}
      </button>
    {/each}
  </nav>

  <div class="panel inset selector-card">
    <label class="label" for="account-scope">Focused account</label>
    <select id="account-scope" bind:value={selectedAccountId}>
      <option value="">All accounts</option>
      {#each accounts as account}
        <option value={account.id}>{account.name} {account.isArchived ? '· archived' : ''}</option>
      {/each}
    </select>
    <p class="hint">Use this scope to keep pockets, contracts and recurring transactions focused.</p>
  </div>

  <div class="stats-grid">
    <article class="stat panel inset"><span>Active accounts</span><strong>{activeAccountCount}</strong></article>
    <article class="stat panel inset"><span>Active pockets</span><strong>{activePocketCount}</strong></article>
    <article class="stat panel inset"><span>Contracts</span><strong>{activeContractCount}</strong></article>
    <article class="stat panel inset"><span>Recurring</span><strong>{activeRecurringCount}</strong></article>
  </div>

  <div class="timeline panel inset">
    <p class="eyebrow">Suggested flow</p>
    <ol>
      <li>Create or update currencies if needed.</li>
      <li>Create an account with institution and weekend handling.</li>
      <li>Add one or more pockets inside that account.</li>
      <li>Attach contracts, partners and recurring transactions.</li>
    </ol>
  </div>
</aside>

<style>
  .panel { background: transparent; border: 0; box-shadow: none; border-radius: 0; backdrop-filter: none; }
  .inset { background: transparent; }
  .sidebar { position: sticky; top: 1.5rem; height: calc(100vh - 3rem); padding: 0 1.5rem 0 0; display: grid; align-content: start; gap: 1.25rem; border-right: 1px solid rgba(148, 163, 184, 0.18); }
  .brand { display: flex; align-items: center; gap: 0.9rem; }
  .brand-mark { width: 3rem; height: 3rem; display: grid; place-items: center; border-radius: 1rem; background: linear-gradient(135deg, #38bdf8, #8b5cf6); color: white; font-weight: 800; letter-spacing: 0.08em; }
  .eyebrow { margin: 0 0 0.35rem; text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.72rem; color: #7dd3fc; }
  h1,p { margin: 0; }
  h1 { font-size: 1.45rem; }
  .muted,.hint,li { color: #b6c6dc; line-height: 1.6; }
  .nav { display: grid; gap: 0.55rem; }
  .nav button { text-align: left; color: #d8e4f6; border: 0; border-left: 2px solid transparent; background: transparent; border-radius: 0; padding: 0.65rem 0.75rem; font-weight: 600; cursor: pointer; }
  .nav button.active { border-left-color: rgba(125, 211, 252, 0.85); color: #7dd3fc; }
  .selector-card,.timeline { padding: 0; }
  .label { display: block; margin-bottom: 0.45rem; font-size: 0.9rem; color: #d8e4f6; font-weight: 600; }
  select { width: 100%; border: 0; border-bottom: 1px solid rgba(148, 163, 184, 0.2); border-radius: 0; background: transparent; color: #f8fbff; padding: 0.75rem 0; font: inherit; color-scheme: dark; }
  select option { background: #0f172a; color: #e5eefb; }
  .stats-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.75rem; }
  .stat { padding: 0.25rem 0; }
  .stat span { display: block; color: #94a3b8; margin-bottom: 0.35rem; font-size: 0.86rem; }
  .stat strong { font-size: 1.55rem; }
  ol { padding-left: 1.1rem; margin: 0.75rem 0 0; display: grid; gap: 0.55rem; }
  @media (max-width: 1180px) { .sidebar { position: static; height: auto; padding: 0 0 1rem 0; border-right: 0; border-bottom: 1px solid rgba(148, 163, 184, 0.18); } }
</style>
