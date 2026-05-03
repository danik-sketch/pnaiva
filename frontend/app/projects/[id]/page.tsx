"use client";

import { useState, use } from "react";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Separator } from "@/components/ui/separator";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  Plus,
  ArrowLeft,
  Calendar,
  Bell,
  CheckSquare,
  Trash2,
  FolderKanban,
} from "lucide-react";
import Link from "next/link";
import useSWR, { mutate } from "swr";
import { projectsApi, tasksApi, remindersApi, categoriesApi } from "@/lib/api";
import type { Task, Reminder, Category } from "@/lib/types";
import { Spinner } from "@/components/ui/spinner";

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function ProjectDetailPage({ params }: PageProps) {
  const { id } = use(params);
  const projectId = parseInt(id);

  const [isAddTaskOpen, setIsAddTaskOpen] = useState(false);
  const [isAddReminderOpen, setIsAddReminderOpen] = useState(false);
  const [selectedTaskForReminder, setSelectedTaskForReminder] = useState<Task | null>(null);
  const [deletingTask, setDeletingTask] = useState<Task | null>(null);
  const [deletingReminder, setDeletingReminder] = useState<Reminder | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const { data: project, error, isLoading: isFetching } = useSWR(
      `project-${projectId}`,
      () => projectsApi.getById(projectId)
  );

  const { data: categories } = useSWR("categories-all", () => categoriesApi.getAll());

  const handleAddTask = async (formData: FormData) => {
    const title = formData.get("title") as string;
    const description = formData.get("description") as string;
    const dueDate = formData.get("dueDate") as string;
    const categoryIds = formData.getAll("categoryIds") as string[];

    if (!title || !dueDate) return;

    setIsLoading(true);
    try {
      await tasksApi.create({
        title,
        description,
        dueDate: new Date(dueDate).toISOString(),
        projectId,
        categoryIds: categoryIds.map((id) => parseInt(id)),
      });
      mutate(`project-${projectId}`);
      mutate((key) => typeof key === "string" && key.startsWith("tasks"));
      setIsAddTaskOpen(false);
    } catch {
      console.error("Failed to add task");
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddReminder = async (formData: FormData) => {
    if (!selectedTaskForReminder) return;

    const message = formData.get("message") as string;
    const reminderTime = formData.get("reminderTime") as string;

    if (!message || !reminderTime) return;

    setIsLoading(true);
    try {
      await remindersApi.create({
        message,
        reminderTime: new Date(reminderTime).toISOString(),
        taskId: selectedTaskForReminder.id,
      });
      mutate(`project-${projectId}`);
      mutate("reminders");
      setIsAddReminderOpen(false);
      setSelectedTaskForReminder(null);
    } catch {
      console.error("Failed to add reminder");
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleTask = async (task: Task) => {
    setIsLoading(true);
    try {
      let categoryIds: number[] | undefined = undefined;

      if (task.categories && categories) {
        categoryIds = [];
        for (const catName of task.categories) {
          const found = categories.find((c) => c.title === catName || c.name === catName);
          if (found) {
            categoryIds.push(found.id);
          }
        }
      }

      const finalCategoryIds = categoryIds === undefined ? undefined : categoryIds;

      await tasksApi.update(task.id, {
        title: task.title,
        description: task.description,
        dueDate: task.dueDate,
        completed: task.completed ? null : new Date().toISOString(),
        projectId: Number(projectId),
        categoryIds: finalCategoryIds,
      });

      await mutate(`project-${projectId}`);
      await mutate((key) => typeof key === "string" && key.startsWith("tasks"));

    } catch (err) {
      console.error("Failed to toggle task:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteTask = async (): Promise<void> => {
    if (!deletingTask) return;

    setIsLoading(true);
    try {
      await tasksApi.delete(deletingTask.id);
      mutate(`project-${projectId}`);
      mutate((key) => typeof key === "string" && key.startsWith("tasks"));
      setDeletingTask(null);
    } catch {
      console.error("Failed to delete task");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteReminder = async (): Promise<void> => {
    if (!deletingReminder) return;

    setIsLoading(true);
    try {
      await remindersApi.delete(deletingReminder.id);
      mutate(`project-${projectId}`);
      mutate("reminders");
      setDeletingReminder(null);
    } catch {
      console.error("Failed to delete reminder");
    } finally {
      setIsLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return "No date";
    return new Date(dateString).toLocaleDateString("ru-RU", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (error) {
    return (
        <DashboardLayout>
          <div className="space-y-6">
            <Link href="/projects">
              <Button variant="ghost" size="sm">
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Projects
              </Button>
            </Link>
            <Card className="border-destructive">
              <CardContent className="pt-6">
                <p className="text-destructive">Failed to load project.</p>
              </CardContent>
            </Card>
          </div>
        </DashboardLayout>
    );
  }

  if (isFetching || !project) {
    return (
        <DashboardLayout>
          <div className="flex items-center justify-center py-12">
            <Spinner className="h-8 w-8" />
          </div>
        </DashboardLayout>
    );
  }

  const completedTasks = project.tasks?.filter((t: Task) => t.completed) ?? [];
  const pendingTasks = project.tasks?.filter((t: Task) => !t.completed) ?? [];
  const totalReminders = project.tasks?.reduce(
      (acc: number, task: Task) => acc + (task.reminders?.length ?? 0),
      0
  ) ?? 0;

  return (
      <DashboardLayout>
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Link href="/projects">
                <Button variant="ghost" size="sm" className="mb-2">
                  <ArrowLeft className="mr-2 h-4 w-4" />
                  Back to Projects
                </Button>
              </Link>
              <div className="flex items-center gap-3">
                <FolderKanban className="h-8 w-8 text-primary" />
                <div>
                  <h1 className="text-3xl font-bold tracking-tight">{project.name}</h1>
                  <p className="mt-1 text-muted-foreground">
                    {project.description || "No description"}
                  </p>
                </div>
              </div>
            </div>
            <Dialog open={isAddTaskOpen} onOpenChange={setIsAddTaskOpen}>
              <DialogTrigger asChild>
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Add Task
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-md">
                <form action={handleAddTask}>
                  <DialogHeader>
                    <DialogTitle>Add Task to Project</DialogTitle>
                    <DialogDescription>
                      Create a new task for &quot;{project.name}&quot;
                    </DialogDescription>
                  </DialogHeader>
                  <div className="space-y-4 py-4">
                    <div className="space-y-2">
                      <Label htmlFor="title">Title</Label>
                      <Input id="title" name="title" placeholder="Task title" required />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="description">Description</Label>
                      <Textarea id="description" name="description" placeholder="Task description" />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="dueDate">Due Date</Label>
                      <Input id="dueDate" name="dueDate" type="datetime-local" required />
                    </div>
                    <div className="space-y-2">
                      <Label>Categories</Label>
                      <div className="grid grid-cols-2 gap-2">
                        {categories?.map((category) => (
                            <label
                                key={category.id}
                                className="flex items-center gap-2 rounded-md border p-2 text-sm"
                            >
                              <Checkbox name="categoryIds" value={category.id.toString()} />
                              {category.name}
                            </label>
                        ))}
                      </div>
                    </div>
                  </div>
                  <DialogFooter>
                    <Button type="button" variant="outline" onClick={() => setIsAddTaskOpen(false)}>
                      Cancel
                    </Button>
                    <Button type="submit" disabled={isLoading}>
                      {isLoading && <Spinner className="mr-2" />}
                      Add Task
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          </div>

          {}
          <div className="grid gap-4 sm:grid-cols-4">
            <Card><CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-muted-foreground">Total Tasks</CardTitle></CardHeader><CardContent><div className="text-2xl font-bold">{project.tasks?.length ?? 0}</div></CardContent></Card>
            <Card><CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-muted-foreground">Completed</CardTitle></CardHeader><CardContent><div className="text-2xl font-bold text-green-500">{completedTasks.length}</div></CardContent></Card>
            <Card><CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-muted-foreground">Pending</CardTitle></CardHeader><CardContent><div className="text-2xl font-bold text-orange-500">{pendingTasks.length}</div></CardContent></Card>
            <Card><CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-muted-foreground">Reminders</CardTitle></CardHeader><CardContent><div className="text-2xl font-bold text-blue-500">{totalReminders}</div></CardContent></Card>
          </div>

          {}
          <Card>
            <CardHeader>
              <CardTitle>Tasks</CardTitle>
              <CardDescription>Manage tasks and reminders</CardDescription>
            </CardHeader>
            <CardContent>
              {(!project.tasks || project.tasks.length === 0) ? (
                  <div className="flex flex-col items-center justify-center py-8">
                    <CheckSquare className="h-12 w-12 text-muted-foreground" />
                    <h3 className="mt-4 text-lg font-semibold">No tasks yet</h3>
                  </div>
              ) : (
                  <Accordion type="multiple" className="w-full">
                    {project.tasks.map((task: Task) => (
                        <AccordionItem key={task.id} value={task.id.toString()} className="border-none">
                          <div className="flex items-center gap-3 w-full border-b px-4 hover:bg-muted/50 transition-colors">
                            <div className="flex items-center py-4">
                              <Checkbox
                                  checked={!!task.completed}
                                  onCheckedChange={() => handleToggleTask(task)}
                              />
                            </div>
                            <AccordionTrigger className="flex-1 py-4 hover:no-underline border-none">
                              <div className="flex items-center gap-3 w-full">
                          <span className={task.completed ? "line-through opacity-60 text-left" : "text-left"}>
                            {task.title}
                          </span>
                                <div className="flex gap-1 flex-wrap">
                                  {task.categories?.map((cat: any, idx: number) => (
                                      <Badge key={idx} variant="secondary" className="text-[10px]">
                                        {typeof cat === 'object' ? (cat.name || cat.title) : cat}
                                      </Badge>
                                  ))}
                                </div>
                                {task.reminders && task.reminders.length > 0 && (
                                    <Badge variant="outline" className="ml-auto shrink-0">
                                      <Bell className="mr-1 h-3 w-3" />
                                      {task.reminders.length}
                                    </Badge>
                                )}
                              </div>
                            </AccordionTrigger>
                          </div>
                          <AccordionContent>
                            <div className="space-y-4 pl-12 pr-4 pt-4">
                              {task.description && <p className="text-sm text-muted-foreground">{task.description}</p>}
                              <div className="flex gap-4 text-sm text-muted-foreground">
                                <div className="flex items-center gap-1"><Calendar className="h-4 w-4" />Due: {formatDate(task.dueDate)}</div>
                              </div>
                              <Separator />
                              <div className="space-y-3">
                                <div className="flex items-center justify-between">
                                  <h4 className="text-sm font-medium">Reminders</h4>
                                  <Button variant="outline" size="sm" onClick={() => { setSelectedTaskForReminder(task); setIsAddReminderOpen(true); }}>
                                    <Plus className="mr-2 h-3 w-3" /> Add Reminder
                                  </Button>
                                </div>
                                {task.reminders?.map((r: Reminder) => (
                                    <div key={r.id} className="flex items-center justify-between border rounded-lg p-2 bg-card">
                                      <div className="text-sm">
                                        <p className="font-medium">{r.message}</p>
                                        <p className="text-xs text-muted-foreground">{formatDate(r.reminderTime)}</p>
                                      </div>
                                      <Button variant="ghost" size="icon" className="h-8 w-8 text-destructive" onClick={() => setDeletingReminder(r)}>
                                        <Trash2 className="h-4 w-4" />
                                      </Button>
                                    </div>
                                ))}
                              </div>
                              <Separator />
                              <div className="flex justify-end">
                                <Button variant="ghost" size="sm" className="text-destructive" onClick={() => setDeletingTask(task)}>
                                  <Trash2 className="mr-2 h-4 w-4" /> Delete Task
                                </Button>
                              </div>
                            </div>
                          </AccordionContent>
                        </AccordionItem>
                    ))}
                  </Accordion>
              )}
            </CardContent>
          </Card>
        </div>

        {}
        <Dialog open={isAddReminderOpen} onOpenChange={(open) => { setIsAddReminderOpen(open); if (!open) setSelectedTaskForReminder(null); }}>
          <DialogContent>
            <form action={handleAddReminder}>
              <DialogHeader><DialogTitle>Add Reminder</DialogTitle></DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2"><Label htmlFor="message">Message</Label><Textarea id="message" name="message" required /></div>
                <div className="space-y-2"><Label htmlFor="reminderTime">Time</Label><Input id="reminderTime" name="reminderTime" type="datetime-local" required /></div>
              </div>
              <DialogFooter>
                <Button type="submit" disabled={isLoading}>{isLoading && <Spinner className="mr-2" />}Save</Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>

        <AlertDialog open={!!deletingTask} onOpenChange={() => setDeletingTask(null)}>
          <AlertDialogContent>
            <AlertDialogHeader><AlertDialogTitle>Delete Task?</AlertDialogTitle></AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction onClick={handleDeleteTask} className="bg-destructive">Delete</AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>

        <AlertDialog open={!!deletingReminder} onOpenChange={() => setDeletingReminder(null)}>
          <AlertDialogContent>
            <AlertDialogHeader><AlertDialogTitle>Delete Reminder?</AlertDialogTitle></AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction onClick={handleDeleteReminder} className="bg-destructive">Delete</AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </DashboardLayout>
  );
}