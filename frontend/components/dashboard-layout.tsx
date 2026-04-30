"use client";

import { AppSidebar } from "./app-sidebar";
import { AuthGuard } from "./auth-guard";

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  return (
    <AuthGuard>
      <div className="flex h-screen bg-background">
        <AppSidebar />
        <main className="flex-1 overflow-auto">
          <div className="container mx-auto p-6">
            {children}
          </div>
        </main>
      </div>
    </AuthGuard>
  );
}
