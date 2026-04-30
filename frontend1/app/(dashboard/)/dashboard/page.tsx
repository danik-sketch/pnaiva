                      <div className="flex items-center gap-2 mt-1">
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
                          <span className="flex items-center text-xs text-muted-foreground">
                            <Calendar className="mr-1 h-3 w-3" />
                            {new Date(task.dueDate).toLocaleDateString()}
                          </span>
                        )}
                      </div>
