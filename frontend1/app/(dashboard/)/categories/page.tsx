                    <div className="flex-1 min-w-0">
                      <p className="font-medium">{category.title}</p>
                      <p className="text-xs text-muted-foreground">
                        {category.tasks?.length || 0} tasks assigned
                      </p>
                      {category.tasks && category.tasks.length > 0 && (
                        <div className="mt-2 space-y-1">
                          {category.tasks.slice(0, 3).map((task, idx) => (
                            <div key={idx} className="text-xs p-1 rounded bg-accent/30">
                              • {task}
                            </div>
                          ))}
                          {category.tasks.length > 3 && (
                            <p className="text-xs text-muted-foreground">
                              +{category.tasks.length - 3} more tasks
                            </p>
                          )}
                        </div>
                      )}
                    </div>
