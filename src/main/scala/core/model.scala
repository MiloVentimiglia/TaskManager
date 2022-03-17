package core

import java.sql.Timestamp

sealed trait Process{
  val priority: Int
  val group: Int
  val UUID: String
  val timestamp: Timestamp
}

case class TaskId(UUID: String, timestamp: Timestamp, priority: Int, group: Int) extends Process
