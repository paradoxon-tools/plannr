<script lang="ts">
  import type { Notice } from '$lib/types';
  import { createEventDispatcher } from 'svelte';

  export let notice: Notice;

  const dispatch = createEventDispatcher<{ close: void }>();
</script>

{#if notice}
  <section class={`notice ${notice.tone}`}>
    <strong>{notice.tone === 'error' ? 'Heads up:' : notice.tone === 'success' ? 'Done:' : 'Info:'}</strong>
    <span>{notice.message}</span>
    <button type="button" class="ghost-icon" on:click={() => dispatch('close')}>×</button>
  </section>
{/if}

<style>
  .notice {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem 1.15rem;
    border-radius: 0.9rem;
    border: 1px solid transparent;
    background: rgba(15, 23, 42, 0.72);
    box-shadow: 0 12px 30px rgba(2, 6, 23, 0.18);
  }
  .notice.info { background: rgba(14, 165, 233, 0.12); border-color: rgba(125, 211, 252, 0.25); }
  .notice.success { background: rgba(34, 197, 94, 0.12); border-color: rgba(134, 239, 172, 0.28); }
  .notice.error { background: rgba(239, 68, 68, 0.14); border-color: rgba(252, 165, 165, 0.3); }
  .ghost-icon {
    margin-left: auto;
    padding: 0.35rem 0.55rem;
    border-radius: 0.5rem;
    cursor: pointer;
    border: 1px solid rgba(148, 163, 184, 0.16);
    background: transparent;
    color: #d8e4f6;
  }
</style>
