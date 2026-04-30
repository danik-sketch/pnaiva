              <CardContent>
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Owner</p>
                    <p className="font-medium">👤 {project.username || "Unknown"}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground mb-2">Tasks ({project.tasks?.length || 0})</p>
                    {project.tasks && project.tasks.length > 0 ? (
                      <div className="space-y-2">
                        {project.tasks.map((task, idx) => (
                          <div key={idx} className="text-sm border rounded p-2 bg-accent/30">
                            <p className="font-medium truncate">{task.title}</p>
                            <div className="flex flex-wrap gap-1 mt-1">
                              {task.categories && task.categories.length > 0 && (
                                <Badge variant="outline" className="text-xs">
                                  🏷️ {task.categories.length} cat
                                </Badge>
                              )}
                              {task.reminders && task.reminders.length > 0 && (
                                <Badge variant="secondary" className="text-xs">
                                  🔔 {task.reminders.length}
                                </Badge>
                              )}
                              {task.dueDate && (
                                <Badge variant="outline" className="text-xs">
                                  📅 {new Date(task.dueDate).toLocaleDateString()}
                                </Badge>
                              )}
                              {task.completed && (
                                <Badge className="text-xs">✅ Done</Badge>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground">No tasks</p>
                    )}
                  </div>
                </div>
              </CardContent>
