"use client";

import { useState, useMemo } from "react";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Plus,
  Pencil,
  Trash2,
  CheckSquare,
  Search,
  Filter,
  X,
  Calendar,
  Bell,
} from "lucide-react";
import useSWR, { mutate } from "swr";
import { tasksApi, projectsApi, categoriesApi, remindersApi } from "@/lib/api";
import type { Task, TaskFilters, Reminder } from "@/lib/types";
import { Spinner } from "@/components/ui/spinner";

export default function TasksPage() {
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<TaskFilters>({});
  const [showFilters, setShowFilters] = useState(false);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [deletingTask, setDeletingTask] = useState<Task | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [reminderTaskId, setReminderTaskId] = useState<number | null>(null);
  const [reminderTime, setReminderTime] = useState("");
  const [reminderMessage, setReminderMessage] = useState("");

  const filterKey = useMemo(() => JSON.stringify(filters), [filters]);

  const { data, error, isLoading: isFetching } = useSWR(
      `tasks-page-${page}-${filterKey}`,
      () => tasksApi.getAll(filters, page, 10)
  );

  const { data: projects } = useSWR("projects-all", () => projectsApi.getAll(0, 100));
  const { data: categories } = useSWR("categories-all", () => categoriesApi.getAll());

  const handleCreate = async (formData: FormData) => {
    const title = formData.get("title") as string;
    const description = formData.get("description") as string;
    const dueDate = formData.get("dueDate") as string;
    const projectId = formData.get("projectId") as string;
    const categoryIds = formData.getAll("categoryIds") as string[];

    if (!title || !dueDate) return;

    setIsLoading(true);
    try {
      await tasksApi.create({
        title,
        description,
        dueDate: new Date(dueDate).toISOString(),
        projectId: projectId ? parseInt(projectId) : undefined,
        categoryIds: categoryIds.map((id) => parseInt(id)),
      });
      await mutate((key) => typeof key === "string" && key.startsWith("tasks"));
      setIsCreateOpen(false);
    } catch {
      console.error("Failed to create task");
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdate = async (formData: FormData) => {
    if (!editingTask) return;

    const title = formData.get("title") as string;
    const description = formData.get("description") as string;
    const dueDate = formData.get("dueDate") as string;
    const projectId = formData.get("projectId") as string;
    const completed = formData.get("completed") === "on";
    const categoryIds = formData.getAll("categoryIds") as string[];

    if (!title || !dueDate) return;

    setIsLoading(true);
    try {
      await tasksApi.update(editingTask.id, {
        title,
        description,
        dueDate: new Date(dueDate).toISOString(),
        completed: completed ? new Date().toISOString() : null,
        projectId: projectId ? parseInt(projectId) : undefined,
        categoryIds: categoryIds.map((id) => parseInt(id)),
      });
      await mutate((key) => typeof key === "string" && key.startsWith("tasks"));
      setEditingTask(null);
    } catch {
      console.error("Failed to update task");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingTask) return;

    setIsLoading(true);
    try {
      await tasksApi.delete(deletingTask.id);
      mutate((key) => typeof key === "string" && key.startsWith("tasks"));
      setDeletingTask(null);
    } catch {
      console.error("Failed to delete task");
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleComplete = async (task: Task) => {
    setIsLoading(true);
    try {
      // Получаем categoryIds из task.categories (массив строк названий)
      // Нам нужно найти ID категорий по их названиям
      const categoryIds: number[] = [];
      if (task.categories && categories) {
        for (const catName of task.categories) {
          const found = categories.find((c) => c.title === catName);
          if (found) {
            categoryIds.push(found.id);
          }
        }
      }

      await tasksApi.update(task.id, {
        title: task.title,
        description: task.description,
        dueDate: task.dueDate,
        completed: task.completed ? null : new Date().toISOString(),
        categoryIds: categoryIds,
      });
      mutate((key) => typeof key === "string" && key.startsWith("tasks"));
    } catch {
      console.error("Failed to toggle task");
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddReminder = async () => {
    if (!reminderTaskId || !reminderTime) return;

    setIsLoading(true);
    try {
      await remindersApi.create({
        taskId: reminderTaskId,
        reminderTime: new Date(reminderTime).toISOString(),
        message: reminderMessage || "Reminder",
      });
      mutate((key) => typeof key === "string" && key.startsWith("tasks"));
      setReminderTaskId(null);
      setReminderTime("");
      setReminderMessage("");
    } catch {
      console.error("Failed to add reminder");
    } finally {
      setIsLoading(false);
    }
  };

  const clearFilters = () => {
    setFilters({});
    setPage(0);
  };

  const hasActiveFilters = Object.values(filters).some((v) => v !== undefined && v !== "");

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("ru-RU", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatDateForInput = (dateString: string) => {
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16);
  };

  return (
      <DashboardLayout>
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">Tasks</h1>
              <p className="mt-1 text-muted-foreground">
                Manage your tasks with filtering and categories
              </p>
            </div>
            <div className="flex gap-2">
              <Button
                  variant={showFilters ? "secondary" : "outline"}
                  onClick={() => setShowFilters(!showFilters)}
              >
                <Filter className="mr-2 h-4 w-4" />
                Filters
                {hasActiveFilters && (
                    <Badge variant="secondary" className="ml-2">
                      Active
                    </Badge>
                )}
              </Button>
              <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                <DialogTrigger asChild>
                  <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    New Task
                  </Button>
                </DialogTrigger>
                <DialogContent className="max-w-md">
                  <form action={handleCreate}>
                    <DialogHeader>
                      <DialogTitle>Create Task</DialogTitle>
                      <DialogDescription>Add a new task to your list</DialogDescription>
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
                        <Label htmlFor="projectId">Project</Label>
                        <Select name="projectId">
                          <SelectTrigger>
                            <SelectValue placeholder="Select a project" />
                          </SelectTrigger>
                          <SelectContent>
                            {projects?.content?.map((project) => (
                                <SelectItem key={project.id} value={project.id.toString()}>
                                  {project.name}
                                </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label>Categories (ManyToMany)</Label>
                        <div className="grid grid-cols-2 gap-2">
                          {categories?.map((category) => (
                              <label
                                  key={category.id}
                                  className="flex items-center gap-2 rounded-md border p-2 text-sm"
                              >
                                <Checkbox name="categoryIds" value={category.id.toString()} />
                                {category.title}
                              </label>
                          ))}
                        </div>
                      </div>
                    </div>
                    <DialogFooter>
                      <Button type="button" variant="outline" onClick={() => setIsCreateOpen(false)}>
                        Cancel
                      </Button>
                      <Button type="submit" disabled={isLoading}>
                        {isLoading && <Spinner className="mr-2" />}
                        Create
                      </Button>
                    </DialogFooter>
                  </form>
                </DialogContent>
              </Dialog>
            </div>
          </div>

          {/* Filters Panel */}
          {showFilters && (
              <Card>
                <CardContent className="pt-6">
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    <div className="space-y-2">
                      <Label htmlFor="filter-title">Title</Label>
                      <div className="relative">
                        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                        <Input
                            id="filter-title"
                            placeholder="Search by title..."
                            className="pl-9"
                            value={filters.title || ""}
                            onChange={(e) =>
                                setFilters((f) => ({ ...f, title: e.target.value || undefined }))
                            }
                        />
                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="filter-date">Due Date</Label>
                      <Input
                          id="filter-date"
                          type="date"
                          value={filters.dueDate || ""}
                          onChange={(e) =>
                              setFilters((f) => ({ ...f, dueDate: e.target.value || undefined }))
                          }
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="filter-status">Status</Label>
                      <Select
                          value={filters.completed === undefined ? "all" : filters.completed.toString()}
                          onValueChange={(v) =>
                              setFilters((f) => ({
                                ...f,
                                completed: v === "all" ? undefined : v === "true",
                              }))
                          }
                      >
                        <SelectTrigger id="filter-status">
                          <SelectValue placeholder="All statuses" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="all">All</SelectItem>
                          <SelectItem value="false">Pending</SelectItem>
                          <SelectItem value="true">Completed</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="flex items-end">
                      <Button variant="ghost" onClick={clearFilters} disabled={!hasActiveFilters}>
                        <X className="mr-2 h-4 w-4" />
                        Clear Filters
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
          )}

          {error && (
              <Card className="border-destructive">
                <CardContent className="pt-6">
                  <p className="text-destructive">Failed to load tasks. Make sure your API is running.</p>
                </CardContent>
              </Card>
          )}

          {isFetching && !data && (
              <div className="flex items-center justify-center py-12">
                <Spinner className="h-8 w-8" />
              </div>
          )}

          {data && data.content.length === 0 && (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <CheckSquare className="h-12 w-12 text-muted-foreground" />
                  <h3 className="mt-4 text-lg font-semibold">No tasks found</h3>
                  <p className="mt-2 text-sm text-muted-foreground">
                    {hasActiveFilters ? "Try adjusting your filters" : "Create your first task to get started"}
                  </p>
                </CardContent>
              </Card>
          )}

          {data && data.content.length > 0 && (
              <Card>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-12"></TableHead>
                      <TableHead>Title</TableHead>
                      <TableHead>Project</TableHead>
                      <TableHead>Categories</TableHead>
                      <TableHead>Due Date</TableHead>
                      <TableHead>Reminders</TableHead>
                      <TableHead className="w-24">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {data.content.map((task) => (
                        <TableRow key={task.id} className={task.completed ? "opacity-60" : ""}>
                          <TableCell>
                            <Checkbox
                                checked={!!task.completed}
                                onCheckedChange={() => handleToggleComplete(task)}
                            />
                          </TableCell>
                          <TableCell>
                            <div>
                              <span className={task.completed ? "line-through" : ""}>{task.title}</span>
                              {task.description && (
                                  <p className="text-sm text-muted-foreground">{task.description}</p>
                              )}
                            </div>
                          </TableCell>
                          <TableCell>
                            {task.projectName ? (
                                <Badge variant="outline">{task.projectName}</Badge>
                            ) : (
                                <span className="text-muted-foreground">-</span>
                            )}
                          </TableCell>
                          <TableCell>
                            <div className="flex flex-wrap gap-1">
                              {task.categories?.map((cat: string) => (
                                  <Badge key={cat} variant="secondary" className="text-xs">
                                    {cat}
                                  </Badge>
                              ))}
                              {(!task.categories || task.categories.length === 0) && (
                                  <span className="text-muted-foreground">-</span>
                              )}
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center gap-1 text-sm">
                              <Calendar className="h-4 w-4 text-muted-foreground" />
                              {formatDate(task.dueDate)}
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center gap-1">
                              {task.reminders && task.reminders.length > 0 && (
                                  <div className="flex items-center gap-1 text-muted-foreground">
                                    <Bell className="h-4 w-4" />
                                    <span className="text-sm">{task.reminders.length}</span>
                                  </div>
                              )}
                              <Popover>
                                <PopoverTrigger asChild>
                                  <Button
                                      variant="ghost"
                                      size="icon"
                                      className="h-7 w-7"
                                      onClick={() => {
                                        setReminderTaskId(task.id);
                                        setReminderTime("");
                                        setReminderMessage("");
                                      }}
                                  >
                                    <Plus className="h-4 w-4" />
                                  </Button>
                                </PopoverTrigger>
                                {/* EVERYTHING BELOW MUST BE INSIDE POPOVERCONTENT */}
                                <PopoverContent className="w-80">
                                  <div className="space-y-4">
                                    <div>
                                      <h4 className="font-medium">Add Reminder</h4>
                                      <p className="text-sm text-muted-foreground">
                                        Set a reminder for this task
                                      </p>
                                    </div>
                                    <div className="space-y-2">
                                      <Label htmlFor="reminder-time">Reminder Time</Label>
                                      <Input
                                          id="reminder-time"
                                          type="datetime-local"
                                          value={reminderTime}
                                          onChange={(e) => setReminderTime(e.target.value)}
                                      />
                                    </div>
                                    <div className="space-y-2">
                                      <Label htmlFor="reminder-message">Message (optional)</Label>
                                      <Input
                                          id="reminder-message"
                                          placeholder="Reminder message"
                                          value={reminderMessage}
                                          onChange={(e) => setReminderMessage(e.target.value)}
                                      />
                                    </div>
                                    <Button
                                        className="w-full"
                                        onClick={handleAddReminder}
                                        disabled={isLoading || !reminderTime}
                                    >
                                      {isLoading && <Spinner className="mr-2" />}
                                      Add Reminder
                                    </Button>
                                    {task.reminders && task.reminders.length > 0 && (
                                        <div className="border-t pt-3">
                                          <p className="text-sm font-medium mb-2">Existing reminders:</p>
                                          <div className="space-y-1">
                                            {task.reminders.map((reminder: Reminder) => (
                                                <div
                                                    key={reminder.id}
                                                    className="flex items-center justify-between text-sm"
                                                >
                                                  <span>{formatDate(reminder.remindAt)}</span>
                                                  <Badge variant={reminder.sent ? "secondary" : "default"}>
                                                    {reminder.sent ? "Sent" : "Pending"}
                                                  </Badge>
                                                </div>
                                            ))}
                                          </div>
                                        </div>
                                    )}
                                  </div>
                                </PopoverContent>
                              </Popover>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="flex gap-1">
                              <Button
                                  variant="ghost"
                                  size="icon"
                                  className="h-8 w-8"
                                  onClick={() => setEditingTask(task)}
                              >
                                <Pencil className="h-4 w-4" />
                              </Button>
                              <Button
                                  variant="ghost"
                                  size="icon"
                                  className="h-8 w-8 text-destructive"
                                  onClick={() => setDeletingTask(task)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Card>
          )}

          {data && data.totalPages > 1 && (
              <div className="flex items-center justify-center gap-2">
                <Button
                    variant="outline"
                    size="sm"
                    disabled={data.first}
                    onClick={() => setPage((p) => p - 1)}
                >
                  Previous
                </Button>
                <span className="text-sm text-muted-foreground">
              Page {page + 1} of {data.totalPages}
            </span>
                <Button
                    variant="outline"
                    size="sm"
                    disabled={data.last}
                    onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </Button>
              </div>
          )}
        </div>

        {/* Edit Dialog */}
        <Dialog open={!!editingTask} onOpenChange={() => setEditingTask(null)}>
          <DialogContent className="max-w-md">
            <form action={handleUpdate}>
              <DialogHeader>
                <DialogTitle>Edit Task</DialogTitle>
                <DialogDescription>Update the task details</DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="edit-title">Title</Label>
                  <Input
                      id="edit-title"
                      name="title"
                      defaultValue={editingTask?.title}
                      required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="edit-description">Description</Label>
                  <Textarea
                      id="edit-description"
                      name="description"
                      defaultValue={editingTask?.description}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="edit-dueDate">Due Date</Label>
                  <Input
                      id="edit-dueDate"
                      name="dueDate"
                      type="datetime-local"
                      defaultValue={editingTask ? formatDateForInput(editingTask.dueDate) : ""}
                      required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="edit-projectId">Project</Label>
                  <Select
                      name="projectId"
                      defaultValue={
                        editingTask?.projectName
                            ? projects?.content?.find(p => p.name === editingTask.projectName)?.id.toString()
                            : undefined
                      }
                  >
                    <SelectTrigger id="edit-projectId">
                      <SelectValue placeholder="Select a project" />
                    </SelectTrigger>
                    <SelectContent>
                      {projects?.content?.map((project) => (
                          <SelectItem key={project.id} value={project.id.toString()}>
                            {project.name}
                          </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label>Categories</Label>
                  <div className="grid grid-cols-2 gap-2">
                    {categories?.map((category) => {
                      const isChecked = editingTask?.categories?.includes(category.name) || false;
                      return (
                          <label
                              key={category.id}
                              className="flex items-center gap-2 rounded-md border p-2 text-sm"
                          >
                            <Checkbox
                                name="categoryIds"
                                value={category.id.toString()}
                                defaultChecked={isChecked}
                            />
                            {category.name}
                          </label>
                      );
                    })}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Checkbox
                      id="edit-completed"
                      name="completed"
                      defaultChecked={!!editingTask?.completed}
                  />
                  <Label htmlFor="edit-completed">Completed</Label>
                </div>
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setEditingTask(null)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading && <Spinner className="mr-2" />}
                  Save
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>

        {/* Delete Dialog */}
        <AlertDialog open={!!deletingTask} onOpenChange={() => setDeletingTask(null)}>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete Task</AlertDialogTitle>
              <AlertDialogDescription>
                Are you sure you want to delete &quot;{deletingTask?.title}&quot;? This action cannot be undone.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction onClick={handleDelete} disabled={isLoading}>
                {isLoading && <Spinner className="mr-2" />}
                Delete
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </DashboardLayout>
  );
}
