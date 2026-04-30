"use client";

import { DashboardLayout } from "@/components/dashboard-layout";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { FolderKanban, CheckSquare, Tags, Bell, ArrowRight } from "lucide-react";
import Link from "next/link";
import useSWR from "swr";
import { projectsApi, tasksApi, categoriesApi, remindersApi } from "@/lib/api";

export default function DashboardPage() {
  const { data: projectsData } = useSWR("projects", () => projectsApi.getAll(0, 100));
  const { data: tasksData } = useSWR("tasks", () => tasksApi.getAll(undefined, 0, 100));
  const { data: categories } = useSWR("categories", () => categoriesApi.getAll());
  const { data: reminders } = useSWR("reminders", () => remindersApi.getAll());

  const stats = [
    {
      name: "Projects",
      value: projectsData?.totalElements ?? 0,
      icon: FolderKanban,
      href: "/projects",
      color: "text-blue-500",
      bgColor: "bg-blue-500/10",
    },
    {
      name: "Tasks",
      value: tasksData?.totalElements ?? 0,
      icon: CheckSquare,
      href: "/tasks",
      color: "text-green-500",
      bgColor: "bg-green-500/10",
    },
    {
      name: "Categories",
      value: categories?.length ?? 0,
      icon: Tags,
      href: "/categories",
      color: "text-orange-500",
      bgColor: "bg-orange-500/10",
    },
    {
      name: "Reminders",
      value: reminders?.length ?? 0,
      icon: Bell,
      href: "/reminders",
      color: "text-purple-500",
      bgColor: "bg-purple-500/10",
    },
  ];

  const completedTasks = tasksData?.content?.filter((t) => t.completed) ?? [];
  const pendingTasks = tasksData?.content?.filter((t) => !t.completed) ?? [];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground">Dashboard</h1>
          <p className="mt-1 text-muted-foreground">
            Overview of your notebook application
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {stats.map((stat) => (
            <Link key={stat.name} href={stat.href}>
              <Card className="transition-colors hover:bg-muted/50">
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="text-sm font-medium text-muted-foreground">
                    {stat.name}
                  </CardTitle>
                  <div className={`rounded-lg p-2 ${stat.bgColor}`}>
                    <stat.icon className={`h-4 w-4 ${stat.color}`} />
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{stat.value}</div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Recent Projects</CardTitle>
              <CardDescription>Your latest projects with tasks</CardDescription>
            </CardHeader>
            <CardContent>
              {projectsData?.content && projectsData.content.length > 0 ? (
                <div className="space-y-4">
                  {projectsData.content.slice(0, 5).map((project) => (
                    <Link
                      key={project.id}
                      href={`/projects/${project.id}`}
                      className="flex items-center justify-between rounded-lg border p-4 transition-colors hover:bg-muted/50"
                    >
                      <div>
                        <p className="font-medium">{project.name}</p>
                        <p className="text-sm text-muted-foreground">
                          {project.tasks?.length ?? 0} tasks
                        </p>
                      </div>
                      <ArrowRight className="h-4 w-4 text-muted-foreground" />
                    </Link>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">No projects yet</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Task Summary</CardTitle>
              <CardDescription>Overview of your tasks</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between rounded-lg bg-green-500/10 p-4">
                  <div className="flex items-center gap-3">
                    <CheckSquare className="h-5 w-5 text-green-500" />
                    <span className="font-medium">Completed</span>
                  </div>
                  <span className="text-2xl font-bold text-green-500">
                    {completedTasks.length}
                  </span>
                </div>
                <div className="flex items-center justify-between rounded-lg bg-orange-500/10 p-4">
                  <div className="flex items-center gap-3">
                    <CheckSquare className="h-5 w-5 text-orange-500" />
                    <span className="font-medium">Pending</span>
                  </div>
                  <span className="text-2xl font-bold text-orange-500">
                    {pendingTasks.length}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Entity Relationships</CardTitle>
            <CardDescription>Data model overview</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <div className="rounded-lg border p-4">
                <h3 className="font-semibold">Project - Task</h3>
                <p className="mt-1 text-sm text-muted-foreground">
                  OneToMany: A project can have multiple tasks
                </p>
              </div>
              <div className="rounded-lg border p-4">
                <h3 className="font-semibold">Task - Category</h3>
                <p className="mt-1 text-sm text-muted-foreground">
                  ManyToMany: Tasks can have multiple categories
                </p>
              </div>
              <div className="rounded-lg border p-4">
                <h3 className="font-semibold">Task - Reminder</h3>
                <p className="mt-1 text-sm text-muted-foreground">
                  OneToMany: A task can have multiple reminders
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
