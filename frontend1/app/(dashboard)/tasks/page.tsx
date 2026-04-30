"use client";

import { useState, useEffect, useCallback } from "react";
import {
  Plus,
  Search,
  Pencil,
  Trash2,
  CheckSquare,
  Square,
  ChevronLeft,
  ChevronRight,
  Loader2,
  Filter,
  X,
  Calendar,
  Tag,
} from "lucide-react";
import { api } from "@/lib/api";
import type { Task, Category, Project, PageResponse } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
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
import { FieldGroup, Field, FieldLabel } from "@/components/ui/field";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { cn } from "@/lib/utils";

export default function TasksPage() {
  const [tasks, setTasks] = useState<PageResponse<Task> | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);

  // Filters
  const [searchQuery, setSearchQuery] = useState("");
  const [filterCompleted, setFilterCompleted] = useState<string>("all");
  const [filterDate, setFilterDate] = useState("");

  // Dialog states
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);

  // Form states
  const [formTitle, setFormTitle] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formDueDate, setFormDueDate] = useState("");
  const [formProjectId, setFormProjectId] = useState<string>("");
  const [formCategoryIds, setFormCategoryIds] = useState<number[]>([]);
  const [formCompleted, setFormCompleted] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const loadTasks = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await api.getTasks(page, 10, {
        title: searchQuery || undefined,
        dueDate: filterDate || undefined,
        completed:
          filterCompleted === "all"
            ? undefined
            : filterCompleted === "completed",
      });
      setTasks(data);
    } catch (error) {
      console.error("Failed to load tasks:", error);
    } finally {
      setIsLoading(false);
    }
  }, [page, searchQuery, filterCompleted, filterDate]);

  const loadProjects = async () => {
    try {
      const data = await api.getProjects(0, 100);
      setProjects(data.content);
    } catch (error) {
      console.error("Failed to load projects:", error);
    }
  };

  const loadCategories = async () => {
    try {
      const data = await api.getCategories();
      setCategories(data);
    } catch (error) {
      console.error("Failed to load categories:", error);
    }
  };

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  useEffect(() => {
    loadProjects();
    loadCategories();
  }, []);

  const handleCreate = async () => {
    setIsSaving(true);
    try {
      await api.createTask({
        title: formTitle,
        description: formDescription,
        dueDate: formDueDate,
        completed: formCompleted,
        projectId: Number(formProjectId),
        categoryIds: formCategoryIds,
      });
      setIsCreateOpen(false);
      resetForm();
      loadTasks();
    } catch (error) {
      console.error("Failed to create task:", error);
    } finally {
      setIsSaving(false);
    }
  };

  const handleEdit = async () => {
    if (!selectedTask) return;
    setIsSaving(true);
    try {
      await api.updateTask(selectedTask.id, {
        title: formTitle,
        description: formDescription,
        dueDate: formDueDate,
        completed: formCompleted,
        projectId: Number(formProjectId),
        categoryIds: formCategoryIds,
      });
      setIsEditOpen(false);
      setSelectedTask(null);
      loadTasks();
    } catch (error) {
      console.error("Failed to update task:", error);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedTask) return;
    try {
      await api.deleteTask(selectedTask.id);
      setIsDeleteOpen(false);
      setSelectedTask(null);
      loadTasks();
    } catch (error) {
      console.error("Failed to delete task:", error);
    }
  };

  const toggleComplete = async (task: Task) => {
    try {
      await api.updateTask(task.id, {
        title: task.title,
        description: task.description,
        dueDate: task.dueDate,
        completed: !task.completed,
        projectId: projects.find((p) => p.name === task.projectName)?.id || 0,
        categoryIds: task.categories.map((c) => c.id),
      });
      loadTasks();
    } catch (error) {
      console.error("Failed to toggle task:", error);
    }
  };

  const resetForm = () => {
    setFormTitle("");
    setFormDescription("");
    setFormDueDate("");
    setFormProjectId("");
    setFormCategoryIds([]);
    setFormCompleted(false);
  };

  const openEditDialog = (task: Task) => {
    setSelectedTask(task);
    setFormTitle(task.title);
    setFormDescription(task.description);
    setFormDueDate(task.dueDate?.split("T")[0] || "");
    setFormProjectId(
      String(projects.find((p) => p.name === task.projectName)?.id || "")
    );
    setFormCategoryIds(task.categories.map((c) => c.id));
    setFormCompleted(task.completed);
    setIsEditOpen(true);
  };

  const openDeleteDialog = (task: Task) => {
    setSelectedTask(task);
    setIsDeleteOpen(true);
  };

  const toggleCategory = (categoryId: number) => {
    setFormCategoryIds((prev) =>
      prev.includes(categoryId)
        ? prev.filter((id) => id !== categoryId)
        : [...prev, categoryId]
    );
  };

  const clearFilters = () => {
    setSearchQuery("");
    setFilterCompleted("all");
    setFilterDate("");
    setPage(0);
  };

  const hasActiveFilters =
    searchQuery || filterCompleted !== "all" || filterDate;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Tasks</h1>
          <p className="text-muted-foreground">Manage and track your tasks</p>
        </div>
        <Button
          onClick={() => {
            resetForm();
            setIsCreateOpen(true);
          }}
        >
          <Plus className="mr-2 h-4 w-4" />
          New Task
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap items-center gap-4">
            <div className="relative flex-1 min-w-[200px] max-w-sm">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search tasks..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  setPage(0);
                }}
                className="pl-10"
              />
            </div>
            <Select
              value={filterCompleted}
              onValueChange={(value) => {
                setFilterCompleted(value);
                setPage(0);
              }}
            >
              <SelectTrigger className="w-[150px]">
                <Filter className="mr-2 h-4 w-4" />
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="pending">Pending</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
              </SelectContent>
            </Select>
            <Input
              type="date"
              value={filterDate}
              onChange={(e) => {
                setFilterDate(e.target.value);
                setPage(0);
              }}
              className="w-[180px]"
            />
            {hasActiveFilters && (
              <Button variant="ghost" size="sm" onClick={clearFilters}>
                <X className="mr-2 h-4 w-4" />
                Clear
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Tasks List */}
      {isLoading ? (
        <div className="flex justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : tasks?.content.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <CheckSquare className="h-12 w-12 text-muted-foreground" />
            <h3 className="mt-4 text-lg font-semibold">No tasks found</h3>
            <p className="text-muted-foreground">
              {hasActiveFilters
                ? "Try adjusting your filters"
                : "Create your first task to get started"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {tasks?.content.map((task) => (
            <Card
              key={task.id}
              className={cn(
                "group transition-colors",
                task.completed && "opacity-60"
              )}
            >
              <CardContent className="flex items-center gap-4 p-4">
                <button
                  onClick={() => toggleComplete(task)}
                  className="flex-shrink-0 text-muted-foreground hover:text-primary"
                >
                  {task.completed ? (
                    <CheckSquare className="h-5 w-5 text-primary" />
                  ) : (
                    <Square className="h-5 w-5" />
                  )}
                </button>
                <div className="flex-1 min-w-0">
                  <h3
                    className={cn(
                      "font-medium",
                      task.completed && "line-through"
                    )}
                  >
                    {task.title}
                  </h3>
                  {task.description && (
                    <p className="text-sm text-muted-foreground line-clamp-1">
                      {task.description}
                    </p>
                  )}
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    {task.projectName && (
                      <Badge variant="outline" className="text-xs">
                        {task.projectName}
                      </Badge>
                    )}
                    {task.categories?.map((category) => (
                      <Badge
                        key={category.id}
                        variant="secondary"
                        className="text-xs"
                      >
                        <Tag className="mr-1 h-3 w-3" />
                        {category.name}
                      </Badge>
                    ))}
                    {task.dueDate && (
                      <span className="flex items-center text-xs text-muted-foreground">
                        <Calendar className="mr-1 h-3 w-3" />
                        {new Date(task.dueDate).toLocaleDateString()}
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8"
                    onClick={() => openEditDialog(task)}
                  >
                    <Pencil className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8 text-destructive"
                    onClick={() => openDeleteDialog(task)}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Pagination */}
      {tasks && tasks.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="icon"
            disabled={tasks.first}
            onClick={() => setPage((p) => p - 1)}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {tasks.pageable.pageNumber + 1} of {tasks.totalPages}
          </span>
          <Button
            variant="outline"
            size="icon"
            disabled={tasks.last}
            onClick={() => setPage((p) => p + 1)}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      )}

      {/* Create/Edit Dialog */}
      <Dialog
        open={isCreateOpen || isEditOpen}
        onOpenChange={(open) => {
          if (!open) {
            setIsCreateOpen(false);
            setIsEditOpen(false);
            setSelectedTask(null);
          }
        }}
      >
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{isEditOpen ? "Edit Task" : "Create Task"}</DialogTitle>
          </DialogHeader>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="title">Title</FieldLabel>
              <Input
                id="title"
                value={formTitle}
                onChange={(e) => setFormTitle(e.target.value)}
                placeholder="Task title"
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="description">Description</FieldLabel>
              <Textarea
                id="description"
                value={formDescription}
                onChange={(e) => setFormDescription(e.target.value)}
                placeholder="Task description"
                rows={3}
              />
            </Field>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="project">Project</FieldLabel>
                <Select value={formProjectId} onValueChange={setFormProjectId}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select project" />
                  </SelectTrigger>
                  <SelectContent>
                    {projects.map((project) => (
                      <SelectItem key={project.id} value={String(project.id)}>
                        {project.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
              <Field>
                <FieldLabel htmlFor="dueDate">Due Date</FieldLabel>
                <Input
                  id="dueDate"
                  type="date"
                  value={formDueDate}
                  onChange={(e) => setFormDueDate(e.target.value)}
                />
              </Field>
            </div>
            <Field>
              <FieldLabel>Categories (ManyToMany)</FieldLabel>
              <div className="flex flex-wrap gap-2 p-3 rounded-md border bg-muted/50">
                {categories.length === 0 ? (
                  <span className="text-sm text-muted-foreground">
                    No categories available
                  </span>
                ) : (
                  categories.map((category) => (
                    <label
                      key={category.id}
                      className={cn(
                        "flex items-center gap-2 px-3 py-1.5 rounded-full border cursor-pointer transition-colors",
                        formCategoryIds.includes(category.id)
                          ? "bg-primary text-primary-foreground border-primary"
                          : "bg-background hover:bg-accent"
                      )}
                    >
                      <Checkbox
                        checked={formCategoryIds.includes(category.id)}
                        onCheckedChange={() => toggleCategory(category.id)}
                        className="sr-only"
                      />
                      {category.name}
                    </label>
                  ))
                )}
              </div>
            </Field>
            <Field>
              <label className="flex items-center gap-2">
                <Checkbox
                  checked={formCompleted}
                  onCheckedChange={(checked) =>
                    setFormCompleted(checked as boolean)
                  }
                />
                <span className="text-sm">Mark as completed</span>
              </label>
            </Field>
          </FieldGroup>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setIsCreateOpen(false);
                setIsEditOpen(false);
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={isEditOpen ? handleEdit : handleCreate}
              disabled={!formTitle || !formProjectId || isSaving}
            >
              {isSaving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {isEditOpen ? "Save" : "Create"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Dialog */}
      <AlertDialog open={isDeleteOpen} onOpenChange={setIsDeleteOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Task</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &quot;{selectedTask?.title}&quot;? This
              action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
