import './globals.css';
import type { Metadata } from 'next';
import { AppLayout } from '../components/AppLayout';

export const metadata: Metadata = {
  title: 'Support Ticket System',
  description: 'Manage and track support tickets',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <AppLayout>{children}</AppLayout>
      </body>
    </html>
  );
}
