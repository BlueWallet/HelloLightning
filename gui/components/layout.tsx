import Head from 'next/head';

const name = 'Hello, Lightning!';
export const siteTitle = 'Hello, Lightning!';

export default function Layout({
  children,
}: {
  children: React.ReactNode
  index?: boolean,
}) {
  return (
    <div>
      <Head>
        <link rel="icon" type="image/png" href="/favicon.png" />
        <meta
          name="description"
          content={name}
        />
        <meta
          property="og:image"
          content={`https://og-image.vercel.app/${encodeURI(
            siteTitle,
          )}.png?theme=light&md=0&fontSize=75px&images=https%3A%2F%2Fassets.zeit.co%2Fimage%2Fupload%2Ffront%2Fassets%2Fdesign%2Fnextjs-black-logo.svg`}
        />
        <meta name="og:title" content={siteTitle} />
        <meta name="twitter:card" content="summary_large_image" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
      </Head>

      <main>{children}</main>
    </div>
  );
}
