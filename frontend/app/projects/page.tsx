"use client";

import { useState } from "react";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
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
import { Plus, Pencil, Trash2, FolderKanban, ArrowRight } from "lucide-react";
import Link from "next/link";
import useSWR, { mutate } from "swr";
import { projectsApi } from "@/lib/api";
// ИСПРАВЛЕНО: Импортируем правильные имена типов из вашего lib/types
import type { Project, ProjectRequest } from "@/lib/types";
import { Spinner } from "@/components/ui/spinner";

export default function ProjectsPage() {
  const [page, setPage] = useState(0);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  // ИСПРАВЛЕНО: Используем тип Project вместо ProjectResponseDto
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [deletingProject, setDeletingProject] = useState<Project | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const { data, error, isLoading: isFetching } = useSWR(
      `projects-page-${page}`,
      () => projectsApi.getAll(page, 10)
  );

  const handleCreate = async (formData: FormData) => {
    const name = formData.get("name") as string;
    const description = formData.get("description") as string;

    if (!name) return;

    setIsLoading(true);
    try {
      await projectsApi.create({
        name,
        description,
      });
      mutate((key) => typeof key === "string" && key.startsWith("projects"));
      setIsCreateOpen(false);
    } catch {
      console.error("Failed to create project");
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdate = async (formData: FormData) => {
    if (!editingProject) return;

    const name = formData.get("name") as string;
    const description = formData.get("description") as string;

    if (!name) return;

    setIsLoading(true);
    try {
      await projectsApi.update(editingProject.id, {
        name,
        description,
      });
      mutate((key) => typeof key === "string" && key.startsWith("projects"));
      setEditingProject(null);
    } catch {
      console.error("Failed to update project");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingProject) return;

    setIsLoading(true);
    try {
      await projectsApi.delete(deletingProject.id);
      mutate((key) => typeof key === "string" && key.startsWith("projects"));
      setDeletingProject(null);
    } catch {
      console.error("Failed to delete project");
    } finally {
      setIsLoading(false);
    }
  };

  return (
      <DashboardLayout>
        {/* Весь остальной JSX остается без изменений */}
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">Projects</h1>
              <p className="mt-1 text-muted-foreground">
                Manage your projects and their tasks
              </p>
            </div>
            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
              <DialogTrigger asChild>
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  New Project
                </Button>
              </DialogTrigger>
              <DialogContent>
                <form action={handleCreate}>
                  <DialogHeader>
                    <DialogTitle>Create Project</DialogTitle>
                    <DialogDescription>
                      Add a new project to organize your tasks
                    </DialogDescription>
                  </DialogHeader>
                  <div className="space-y-4 py-4">
                    <div className="space-y-2">
                      <Label htmlFor="name">Name</Label>
                      <Input id="name" name="name" placeholder="Project name" required />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="description">Description</Label>
                      <Textarea
                          id="description"
                          name="description"
                          placeholder="Project description"
                      />
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

          {error && (
              <Card className="border-destructive">
                <CardContent className="pt-6">
                  <p className="text-destructive">Failed to load projects. Make sure your API is running.</p>
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
                  <FolderKanban className="h-12 w-12 text-muted-foreground" />
                  <h3 className="mt-4 text-lg font-semibold">No projects yet</h3>
                  <p className="mt-2 text-sm text-muted-foreground">
                    Create your first project to get started
                  </p>
                </CardContent>
              </Card>
          )}

          {data && data.content.length > 0 && (
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {data.content.map((project) => (
                    <Card key={project.id} className="group relative">
                      <CardHeader>
                        <div className="flex items-start justify-between">
                          <div>
                            <CardTitle className="text-lg">{project.name}</CardTitle>
                            <CardDescription className="mt-1">
                              {project.description || "No description"}
                            </CardDescription>
                          </div>
                          <div className="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => setEditingProject(project)}
                            >
                              <Pencil className="h-4 w-4" />
                            </Button>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8 text-destructive"
                                onClick={() => setDeletingProject(project)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">
                      {project.tasks?.length ?? 0} tasks
                    </span>
                          <Link href={`/projects/${project.id}`}>
                            <Button variant="ghost" size="sm">
                              View
                              <ArrowRight className="ml-2 h-4 w-4" />
                            </Button>
                          </Link>
                        </div>
                      </CardContent>
                    </Card>
                ))}
              </div>
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

        <Dialog open={!!editingProject} onOpenChange={() => setEditingProject(null)}>
          <DialogContent>
            <form action={handleUpdate}>
              <DialogHeader>
                <DialogTitle>Edit Project</DialogTitle>
                <DialogDescription>Update the project details</DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="edit-name">Name</Label>
                  <Input id="edit-name" name="name" defaultValue={editingProject?.name} required />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="edit-description">Description</Label>
                  <Textarea id="edit-description" name="description" defaultValue={editingProject?.description} />
                </div>
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setEditingProject(null)}>Cancel</Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading && <Spinner className="mr-2" />}
                  Update
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>

        <AlertDialog open={!!deletingProject} onOpenChange={() => setDeletingProject(null)}>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Delete Project</AlertDialogTitle>
              <AlertDialogDescription>
                Are you sure you want to delete &quot;{deletingProject?.name}&quot;? This action cannot
                be undone and will also delete all associated tasks.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                  onClick={handleDelete}
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              >
                {isLoading && <Spinner className="mr-2" />}
                Delete
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </DashboardLayout>
  );
}