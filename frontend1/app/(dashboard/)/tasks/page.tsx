                    <div className="flex-1 min-w-0">
                      <p className="font-medium truncate">{task.title}</p>
                      <p className="text-xs text-muted-foreground line-clamp-2">{task.description}</p>
                      <div className="flex flex-wrap gap-1 mt-2">
                        {task.projectName && (
                          <Badge variant="outline" className="text-xs">
                            📁 {task.projectName}
                          </Badge>
                        )}
                        {task.categories && task.categories.length > 0 && (
                          <Badge variant="secondary" className="text-xs">
                            🏷️ {task.categories.length} categories
                          </Badge>
                        )}
                        {task.reminders && task.reminders.length > 0 && (
                          <Badge variant="outline" className="text-xs">
                            🔔 {task.reminders.length} reminders
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
