<script lang="ts">
  let { data } = $props();
</script>

<svelte:head>
  <title>plannr</title>
  <meta name="description" content="plannr frontend" />
</svelte:head>

<section>
  <p class="eyebrow">plannr monorepo</p>
  <h1>SvelteKit frontend is ready</h1>
  <p>
    The frontend now lives in <code>apps/web</code> and can talk to the backend at
    <code>{data.apiBaseUrl}</code>.
  </p>
</section>

<style>
  :global(body) {
    margin: 0;
    font-family: Inter, system-ui, sans-serif;
    background: #0f172a;
    color: #e2e8f0;
  }

  section {
    min-height: 100vh;
    display: grid;
    place-content: center;
    gap: 1rem;
    padding: 2rem;
    text-align: center;
  }

  .eyebrow {
    text-transform: uppercase;
    letter-spacing: 0.12em;
    color: #38bdf8;
  }

  h1 {
    margin: 0;
    font-size: clamp(2rem, 5vw, 4rem);
  }

  p {
    margin: 0 auto;
    max-width: 40rem;
    line-height: 1.6;
  }

  code {
    padding: 0.15rem 0.4rem;
    border-radius: 0.35rem;
    background: rgba(148, 163, 184, 0.15);
  }
</style>
