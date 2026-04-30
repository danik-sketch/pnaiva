"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import {
  FolderKanban,
  CheckSquare,
  Clock,
  ArrowRight,
  Loader2,
  Calendar,
} from "lucide-react";
import { api } from "@/lib/api";
import type { Task, Project } from "@/lib/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface Stats {
  totalProjects: number;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
}

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [recentTasks, setRecentTasks] = useState<Task[]>([]);
  const [recentProjects, setRecentProjects] = useState<Project[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      setIsLoading(true);
      try {
        const [tasksData, projectsData] = await Promise.all([
          api.getTasks(0, 5),
          api.getProjects(0, 5),
        ]);

        const allTasks = await api.getTasks(0, 1000);

        // Проверяем что данные есть
        if (!allTasks || !allTasks.content) {
          console.error("Invalid tasks response:", allTasks);
          throw new Error("Invalid tasks response from server");
        }

        const completedCount = allTasks.content.filter((t) => t.completed).length;

        setStats({
          totalProjects: projectsData.totalElements,
          totalTasks: allTasks.totalElements,
          completedTasks: completedCount,
          pendingTasks: allTasks.totalElements - completedCount,
        });

        setRecentTasks(tasksData.content);
        setRecentProjects(projectsData.content);
      } catch (error) {
        console.error("Failed to load dashboard data:", error);
        // Показываем пустой статус
        setStats({
          totalProjects: 0,
          totalTasks: 0,
          completedTasks: 0,
          pendingTasks: 0,
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const statCards = [
    {
      title: "Total Projects",
      value: stats?.totalProjects || 0,
      icon: FolderKanban,
      href: "/projects",
      color: "text-blue-500",
      bgColor: "bg-blue-500/10",
    },
    {
      title: "Total Tasks",
      value: stats?.totalTasks || 0,
      icon: CheckSquare,
      href: "/tasks",
      color: "text-primary",
      bgColor: "bg-primary/10",
    },
    {
      title: "Pending Tasks",
      value: stats?.pendingTasks || 0,
      icon: Clock,
      href: "/tasks?status=pending",
      color: "text-amber-500",
      bgColor: "bg-amber-500/10",
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Dashboard</h1>
        <p className="text-muted-foreground">
          Overview of your projects and tasks
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {statCards.map((stat) => (
          <Link key={stat.title} href={stat.href}>
            <Card className="transition-colors hover:bg-accent/50">
              <CardContent className="flex items-center gap-4 p-6">
                <div className={cn("rounded-lg p-3", stat.bgColor)}>
                  <stat.icon className={cn("h-6 w-6", stat.color)} />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">{stat.title}</p>
                  <p className="text-2xl font-bold">{stat.value}</p>
                </div>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>

      {/* Progress Card */}
      <Card>
        <CardHeader>
          <CardTitle>Task Completion Progress</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4">
            <div className="flex-1">
              <div className="h-3 rounded-full bg-muted overflow-hidden">
                <div
                  className="h-full rounded-full bg-primary transition-all"
                  style={{
                    width: `${
                      stats?.totalTasks
                        ? (stats.completedTasks / stats.totalTasks) * 100
                        : 0
                    }%`,
                  }}
                />
              </div>
            </div>
            <span className="text-sm font-medium">
              {stats?.completedTasks || 0} / {stats?.totalTasks || 0} completed
            </span>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Recent Tasks */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Recent Tasks</CardTitle>
            <Button variant="ghost" size="sm" asChild>
              <Link href="/tasks">
                View all
                <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
          </CardHeader>
          <CardContent>
            {recentTasks.length === 0 ? (
              <p className="text-center text-muted-foreground py-4">
                No tasks yet
              </p>
            ) : (
              <div className="space-y-3">
                {recentTasks.map((task) => (
                  <div
                    key={task.id}
                    className="flex items-center gap-3 rounded-lg border p-3"
                  >
                    <div
                      className={cn(
                        "flex h-8 w-8 items-center justify-center rounded-full",
                        task.completed
                          ? "bg-primary/10 text-primary"
                          : "bg-muted text-muted-foreground"
                      )}
                    >
                      <CheckSquare className="h-4 w-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p
                        className={cn(
                          "font-medium truncate",
                          task.completed && "line-through text-muted-foreground"
                        )}
                      >
                        {task.title}
                      </p>
                      <div className="flex items-center gap-2 mt-1">
                        {task.projectName && (
                          <Badge variant="outline" className="text-xs">
                            {task.projectName}
                          </Badge>
                        )}
                        {task.dueDate && (
                          <span className="flex items-center text-xs text-muted-foreground">
                            <Calendar className="mr-1 h-3 w-3" />
                            {new Date(task.dueDate).toLocaleDateString()}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Recent Projects */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Recent Projects</CardTitle>
            <Button variant="ghost" size="sm" asChild>
              <Link href="/projects">
                View all
                <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
          </CardHeader>
          <CardContent>
            {recentProjects.length === 0 ? (
              <p className="text-center text-muted-foreground py-4">
                No projects yet
              </p>
            ) : (
              <div className="space-y-3">
                {recentProjects.map((project) => (
                  <div
                    key={project.id}
                    className="flex items-center gap-3 rounded-lg border p-3"
                  >
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-500/10 text-blue-500">
                      <FolderKanban className="h-4 w-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium truncate">{project.name}</p>
                      <p className="text-xs text-muted-foreground truncate">
                        {project.description || "No description"}
                      </p>
                    </div>
                    <Badge variant="secondary">
                      {project.tasks?.length || 0} tasks
                    </Badge>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
