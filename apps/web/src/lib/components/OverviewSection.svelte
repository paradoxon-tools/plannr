<script lang="ts">
  import type { Account } from '$lib/types';
  export let selectedAccount: Account | null = null;
  export let lastSyncedAt = '';
  export let accountsCount = 0;
  export let pocketsCount = 0;
  export let contractsCount = 0;
  export let recurringCount = 0;
  export let openAccounts: () => void;
  export let openPockets: () => void;
</script>

<section class="overview-grid">
  <article class="feature panel">
    <p class="eyebrow">Getting started</p>
    <h3>{selectedAccount ? selectedAccount.name : 'Create your first account'}</h3>
    <p>
      {#if selectedAccount}
        Focused on <strong>{selectedAccount.institution}</strong> with currency <strong>{selectedAccount.currencyCode}</strong>.
      {:else}
        No account selected yet. Start by creating one below.
      {/if}
    </p>
    <div class="cta-row">
      <button type="button" class="primary" on:click={openAccounts}>Account form</button>
      <button type="button" class="secondary" on:click={openPockets}>Pocket form</button>
    </div>
  </article>

  <article class="feature panel spotlight">
    <p class="eyebrow">Last sync</p>
    <h3>{lastSyncedAt || 'Not synced yet'}</h3>
    <p>Data is fetched live from the backend and merged across active and archived records.</p>
    <div class="pill-row">
      <span class="pill">{accountsCount} accounts</span>
      <span class="pill">{pocketsCount} pockets</span>
      <span class="pill">{contractsCount} contracts</span>
      <span class="pill">{recurringCount} recurring</span>
    </div>
  </article>
</section>

<style>
  .overview-grid { display:grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap:1.5rem; }
  .panel { background: transparent; border: 0; box-shadow: none; border-radius: 0; backdrop-filter: none; }
  .feature { padding:0 0 1.25rem; border-bottom: 1px solid rgba(148, 163, 184, 0.18); }
  .spotlight { background: transparent; }
  .eyebrow { margin: 0 0 0.35rem; text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.72rem; color: #7dd3fc; }
  h3,p { margin:0; }
  h3 { font-size:1.35rem; }
  p { color:#b6c6dc; line-height:1.6; }
  .cta-row,.pill-row { display:flex; gap:.75rem; flex-wrap:wrap; margin-top:1rem; }
  .pill { display:inline-flex; padding:0; border-radius:0; background: transparent; color:#e2e8f0; font-size:.88rem; }
  button { font:inherit; border-radius:.95rem; cursor:pointer; }
  .primary { padding:.85rem 1.15rem; background:linear-gradient(135deg,#38bdf8,#8b5cf6); color:white; font-weight:700; border:1px solid transparent; }
  .secondary { padding:.78rem 1.05rem; background:rgba(148,163,184,.12); border:1px solid rgba(148,163,184,.16); color:#edf5ff; font-weight:600; }
  @media (max-width: 860px) { .overview-grid { grid-template-columns:1fr; } }
</style>
