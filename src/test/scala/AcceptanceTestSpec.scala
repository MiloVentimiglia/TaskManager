import core._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.{BeforeAndAfter, GivenWhenThen}
import org.scalatest.matchers.should.Matchers

import scala.collection.immutable.Queue
import java.io.ByteArrayOutputStream
import java.sql.Timestamp


class AcceptanceTestSpec extends AnyFeatureSpec with GivenWhenThen with Matchers with BeforeAndAfter{

  val q: Queue[TaskId] = Queue(
      TaskId("Event1", Timestamp.valueOf("2000-01-01 12:00:00"), priority = 4, group = 1),
      TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
      TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
      TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1)
  )



  Scenario("Attempting to create a queue bigger than the maximum allowed size") {
    a[IllegalArgumentException] should be thrownBy Manager(q, 3)
  }



  Scenario("(1/4) - Add a process when the capacity of the TaskManager IS NOT FULL") {
    Given("the object instance")
    val input: Manager = Manager(q, 6)

    When("event 5 is added")
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"),  priority = 1, group = 1)
    val out = input.enqueueFinite(elem)

    Then("we accept new processes as long there is capacity inside the TaskManager")
    out.value should be (
      Queue(
        TaskId("Event1", Timestamp.valueOf("2000-01-01 12:00:00"), priority = 4, group = 1),
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1),
        TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
      )
    )
  }



  Scenario("(1/4) - Add a process when the capacity of the TaskManager IS FULL") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"),  priority = 1, group = 1)
    val out = input.enqueueFinite(elem)

    Then("we won’t accept any new process.")
    out.value should be (
      Queue(
        TaskId("Event1", Timestamp.valueOf("2000-01-01 12:00:00"), priority = 4, group = 1),
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1)
      )
    )
  }



  Scenario("(2/4) - Add a process FIFO approach when the capacity of the TaskManager IS NOT FULL") {
    Given("the object instance")
    val input: Manager = Manager(q, 6)

    When("event 5 is added")
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
    val out = input.enqueueFIFO(elem)

    Then("we accept new processes as long there is capacity inside the TaskManager.")
    out.value should be (
      Queue(
        TaskId("Event1", Timestamp.valueOf("2000-01-01 12:00:00"), priority = 4, group = 1),
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1),
        TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
      )
    )
  }



  Scenario("(2/4) - Add a process FIFO approach when the capacity of the TaskManager IS FULL") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
    val out = input.enqueueFIFO(elem)

    Then("we drop the last element of the queue and add the new.")
    out.value should be (
      Queue(
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1),
        TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
      )
    )
  }



  Scenario("(3/4) - Add a priority-based process when the capacity of the TaskManager IS NOT FULL") {
    Given("the object instance")
    val input: Manager = Manager(q, 6)

    When("event 5 is added")
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
    val out = input.enqueuePriority(elem)

    Then("we accept new processes as long there is capacity inside the TaskManager.")
    out.value should be (
      Queue(
        TaskId("Event1", Timestamp.valueOf("2000-01-01 12:00:00"), priority = 4, group = 1),
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1),
        TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
      )
    )
  }



  Scenario("(3/4) - Add a task with highest priority when the capacity of the TaskManager IS FULL") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 10, group = 1)
    val out = input.enqueuePriority(elem)

    Then("we drop the last element of the queue and add the new.")
    out.value should be (
      Queue(
        TaskId("Event1", Timestamp.valueOf("2000-01-01 12:00:00"), priority = 4, group = 1),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1),
        TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 10, group = 1)
      )
    )
  }



  Scenario("(3/4) - Add a task with low priority when the capacity of the TaskManager IS FULL") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-05 12:00:00"), priority = 1, group = 1)
    val out = input.enqueuePriority(elem)

    Then("we drop the last element of the queue and add the new.")
    out.value should be (
      Queue(
        TaskId("Event1", Timestamp.valueOf("2000-01-01 12:00:00"), priority = 4, group = 1),
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1)
      )
    )
  }



  Scenario("(4/4) - List running processes") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    Console.withOut(out)(input.listProcesses)

    Then("we drop the last element of the queue and add the new.")
    out.toString().trim() should be (
      "Queue(TaskId(Event1,2000-01-01 12:00:00.0,4,1), TaskId(Event2,2000-01-02 12:00:00.0,1,2), TaskId(Event3,2000-01-02 13:00:00.0,1,2), TaskId(Event4,2000-01-03 12:00:00.0,2,1))"
    )
  }



  Scenario("(4/4) - List running processes sorted by priority") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    Console.withOut(out)(input.listProcesses("priority"))

    Then("we drop the last element of the queue and add the new.")
    out.toString().trim() should be (
      "Queue(TaskId(Event2,2000-01-02 12:00:00.0,1,2), TaskId(Event3,2000-01-02 13:00:00.0,1,2), TaskId(Event4,2000-01-03 12:00:00.0,2,1), TaskId(Event1,2000-01-01 12:00:00.0,4,1))"
    )
  }



  Scenario("(4/4) - List running processes sorted by timestamp") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)
    val elem = TaskId("Event5", Timestamp.valueOf("2000-01-01 01:00:00"), priority = 10, group = 1)
    val enrichedInput = input.enqueuePriority(elem)

    When("event 5 is added")
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    Console.withOut(out)(enrichedInput.listProcesses("timestamp"))

    Then("we drop the last element of the queue and add the new.")
    out.toString().trim() should be (
      "Queue(TaskId(Event5,2000-01-01 01:00:00.0,10,1), TaskId(Event1,2000-01-01 12:00:00.0,4,1), TaskId(Event3,2000-01-02 13:00:00.0,1,2), TaskId(Event4,2000-01-03 12:00:00.0,2,1))"
    )
  }



  Scenario("(4/4) - List running processes sorted by group") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    Console.withOut(out)(input.listProcesses("group"))

    Then("we drop the last element of the queue and add the new.")
    out.toString().trim() should be (
      "Queue(TaskId(Event1,2000-01-01 12:00:00.0,4,1), TaskId(Event4,2000-01-03 12:00:00.0,2,1), TaskId(Event2,2000-01-02 12:00:00.0,1,2), TaskId(Event3,2000-01-02 13:00:00.0,1,2))"
    )
  }



  Scenario("Kill process by uuid") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val out = input.killProcess("Event1")

    Then("we drop the last element of the queue and add the new.")
    out.value should be (
      Queue(
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2),
        TaskId("Event4", Timestamp.valueOf("2000-01-03 12:00:00"), priority = 2, group = 1)
      )
    )
  }



  Scenario("Kill all processes") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val out = input.killAll

    Then("we drop the last element of the queue and add the new.")
    out.value should be (Queue.empty[TaskId])
  }



  Scenario("Kill processes by group") {
    Given("the object instance")
    val input: Manager = Manager(q, 4)

    When("event 5 is added")
    val out = input.killProcesses(1)

    Then("we drop the last element of the queue and add the new.")
    out.value should be (
      Queue(
        TaskId("Event2", Timestamp.valueOf("2000-01-02 12:00:00"), priority = 1, group = 2),
        TaskId("Event3", Timestamp.valueOf("2000-01-02 13:00:00"), priority = 1, group = 2)
      )
    )
  }


}
