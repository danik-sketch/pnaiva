"use client";

import { useState } from "react";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Plus, Pencil, Trash2, Bell, Calendar, CheckSquare } from "lucide-react";
import useSWR, { mutate } from "swr";
import { remindersApi, tasksApi } from "@/lib/api";
import type { ReminderResponseDto } from "@/lib/types";
import { Spinner } from "@/components/ui/spinner";
import { Badge } from "@/components/ui/badge";

export default function RemindersPage() {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingReminder, setEditingReminder] = useState<ReminderResponseDto | null>(null);
  const [deletingReminder, setDeletingReminder] = useState<ReminderResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const { data: reminders, error, isLoading: isFetching } = useSWR(
    "reminders",
    () => remindersApi.getAll()
  );

  const { data: tasksData } = useSWR("tasks-all", () => tasksApi.getAll(undefined, 0, 100));

  const handleCreate = async (formData: FormData) => {
    const message = formData.get("message") as string;
    const reminderTime = formData.get("reminderTime") as string;
    const taskId = formData.get("taskId") as string;

    if (!message || !reminderTime || !taskId) return;

    setIsLoading(true);
    try {
      await remindersApi.create({
        message,
        reminderTime: new Date(reminderTime).toISOString(),
        taskId: parseInt(taskId),
      });
      mutate("reminders");
      setIsCreateOpen(false);
    } catch {
      console.error("Failed to create reminder");
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdate = async (formData: FormData) => {
    if (!editingReminder) return;

    const message = formData.get("message") as string;
    const reminderTime = formData.get("reminderTime") as string;
    const taskId = formData.get("taskId") as string;

    if (!message || !reminderTime || !taskId) return;

    setIsLoading(true);
    try {
      await remindersApi.update(editingReminder.id, {
        message,
        reminderTime: new Date(reminderTime).toISOString(),
        taskId: parseInt(taskId),
      });
      mutate("reminders");
      setEditingReminder(null);
    } catch {
      console.error("Failed to update reminder");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingReminder) return;

    setIsLoading(true);
    try {
      await remindersApi.delete(deletingReminder.id);
      mutate("reminders");
      setDeletingReminder(null);
    } catch {
      console.error("Failed to delete reminder");
    } finally {
      setIsLoading(false);
    }
  };

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

  const getTaskTitle = (taskId: number) => {
    const task = tasksData?.content?.find((t) => t.id === taskId);
    return task?.title || `Task #${taskId}`;
  };

  const isUpcoming = (dateString: string) => {
    return new Date(dateString) > new Date();
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Reminders</h1>
            <p className="mt-1 text-muted-foreground">
              Manage reminders for your tasks (OneToMany relationship)
            </p>
          </div>
          <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                New Reminder
              </Button>
            </DialogTrigger>
            <DialogContent>
              <form action={handleCreate}>
                <DialogHeader>
                  <DialogTitle>Create Reminder</DialogTitle>
                  <DialogDescription>Add a new reminder for a task</DialogDescription>
                </DialogHeader>
                <div className="space-y-4 py-4">
                  <div className="space-y-2">
                    <Label htmlFor="taskId">Task</Label>
                    <Select name="taskId" required>
                      <SelectTrigger>
                        <SelectValue placeholder="Select a task" />
                      </SelectTrigger>
                      <SelectContent>
                        {tasksData?.content?.map((task) => (
                          <SelectItem key={task.id} value={task.id.toString()}>
                            {task.title}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="message">Message</Label>
                    <Textarea
                      id="message"
                      name="message"
                      placeholder="Reminder message"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="reminderTime">Reminder Time</Label>
                    <Input
                      id="reminderTime"
                      name="reminderTime"
                      type="datetime-local"
                      required
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
              <p className="text-destructive">Failed to load reminders. Make sure your API is running.</p>
            </CardContent>
          </Card>
        )}

        {isFetching && !reminders && (
          <div className="flex items-center justify-center py-12">
            <Spinner className="h-8 w-8" />
          </div>
        )}

        {reminders && reminders.length === 0 && (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <Bell className="h-12 w-12 text-muted-foreground" />
              <h3 className="mt-4 text-lg font-semibold">No reminders yet</h3>
              <p className="mt-2 text-sm text-muted-foreground">
                Create your first reminder to stay on track
              </p>
            </CardContent>
          </Card>
        )}

        {reminders && reminders.length > 0 && (
          <Card>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-12"></TableHead>
                  <TableHead>Task</TableHead>
                  <TableHead>Message</TableHead>
                  <TableHead>Reminder Time</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="w-24">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {reminders.map((reminder) => (
                  <TableRow key={reminder.id}>
                    <TableCell>
                      <Bell className="h-4 w-4 text-muted-foreground" />
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <CheckSquare className="h-4 w-4 text-muted-foreground" />
                        <span className="font-medium">{getTaskTitle(reminder.taskId)}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <p className="max-w-xs truncate">{reminder.message}</p>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1 text-sm">
                        <Calendar className="h-4 w-4 text-muted-foreground" />
                        {formatDate(reminder.reminderTime)}
                      </div>
                    </TableCell>
                    <TableCell>
                      {isUpcoming(reminder.reminderTime) ? (
                        <Badge variant="secondary">Upcoming</Badge>
                      ) : (
                        <Badge variant="outline">Past</Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8"
                          onClick={() => setEditingReminder(reminder)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-destructive"
                          onClick={() => setDeletingReminder(reminder)}
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

       </div>

      {}
      <Dialog open={!!editingReminder} onOpenChange={() => setEditingReminder(null)}>
        <DialogContent>
          <form action={handleUpdate}>
            <DialogHeader>
              <DialogTitle>Edit Reminder</DialogTitle>
              <DialogDescription>Update the reminder details</DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="edit-taskId">Task</Label>
                <Select name="taskId" defaultValue={editingReminder?.taskId.toString()}>
                  <SelectTrigger id="edit-taskId">
                    <SelectValue placeholder="Select a task" />
                  </SelectTrigger>
                  <SelectContent>
                    {tasksData?.content?.map((task) => (
                      <SelectItem key={task.id} value={task.id.toString()}>
                        {task.title}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-message">Message</Label>
                <Textarea
                  id="edit-message"
                  name="message"
                  defaultValue={editingReminder?.message}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-reminderTime">Reminder Time</Label>
                <Input
                  id="edit-reminderTime"
                  name="reminderTime"
                  type="datetime-local"
                  defaultValue={editingReminder ? formatDateForInput(editingReminder.reminderTime) : ""}
                  required
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setEditingReminder(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading && <Spinner className="mr-2" />}
                Update
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {}
      <AlertDialog open={!!deletingReminder} onOpenChange={() => setDeletingReminder(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Reminder</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this reminder? This action cannot be undone.
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
