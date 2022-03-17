import java.sql.Timestamp
import scala.collection.immutable.Queue

package object core {

  trait HasSideEffect {
    def listProcesses: Unit
    def listProcesses(param: String): Unit
  }

  trait Ops[A] extends HasSideEffect {
    def enqueueFinite(elem: A): Ops[A]
    def enqueueFIFO(elem: A): Ops[A]
    def enqueuePriority(elem: A): Ops[A]
    def killProcess(uuid: String): Ops[A]
    def killProcesses(group: Int): Ops[A]
    def killAll: Ops[A]
    def value: Queue[A]
  }


  class Manager(q: Queue[Process], maxSize: Int) extends Ops[Process] {

    override def enqueueFinite(elem: Process): Manager =
      if (q.size < maxSize)
        Manager(q.enqueue(elem), maxSize)
      else
        Manager(q, maxSize)

    override def enqueueFIFO(elem: Process): Manager =
      if (q.size == maxSize)
        Manager(q.enqueue(elem).dequeue._2, maxSize)
      else
        Manager(q.enqueue(elem), maxSize)

    override def enqueuePriority(elem: Process): Manager = {
      val maxPriority: Option[Int] = q.map(_.priority).maxOption

      if (maxPriority.isDefined && (q.size == maxSize) && (elem.priority > maxPriority.get)) {
        val minPriority: Int = q.map(_.priority).min

        val minTask = q
          .filter(_.priority == minPriority)
          .sortBy(_.timestamp)(Ordering[Timestamp])
          .head

        Manager(q.filter(c => !c.equals(minTask)).enqueue(elem), maxSize)
      } else if (maxPriority.isDefined && (q.size == maxSize) && (elem.priority < maxPriority.get)){
        Manager(q, maxSize)
      } else {
        Manager(q.enqueue(elem), maxSize)
      }
    }

    override def listProcesses: Unit = println(q)

    override def listProcesses(param: String): Unit =
      if (param == "priority") println(q.sortBy(_.priority))
      else if (param == "timestamp") println(q.sortBy(_.timestamp))
      else if (param == "group") println(q.sortBy(_.group))
      else println("Not implemented")

    override def killProcess(uuid: String): Manager = Manager(q.filter(_.UUID != uuid), maxSize)

    override def killProcesses(group: Int): Manager = Manager(q.filter(_.group != group), maxSize)

    override def killAll: Manager = Manager(Queue.empty[Process], maxSize)

    override def value: Queue[Process] = q

  }

  object Manager {
    def apply(q: Queue[Process], maxsize: Int): Manager = {
      require(q.size <= maxsize)
      new Manager(q, maxsize)
    }
  }

}
