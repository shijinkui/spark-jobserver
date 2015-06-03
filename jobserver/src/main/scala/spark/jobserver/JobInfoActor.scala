package spark.jobserver

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config
import spark.jobserver.akka.InstrumentedActor
import spark.jobserver.io.JobDAO

import scala.concurrent.Await

object JobInfoActor {

  // Requests
  case class GetJobStatuses(limit: Option[Int])

  case class GetJobConfig(jobId: String)

  case class StoreJobConfig(jobId: String, jobConfig: Config)

  // Responses
  case object JobConfigStored

}

class JobInfoActor(jobDao: JobDAO, contextSupervisor: ActorRef) extends InstrumentedActor {

  import CommonMessages._
  import JobInfoActor._
  import context.dispatcher

  import scala.concurrent.duration._
  import scala.util.control.Breaks._

  // for futures to work

  // Used in the asks (?) below to request info from contextSupervisor and resultActor
  implicit val ShortTimeout = Timeout(3 seconds)

  override def wrappedReceive: Receive = {
    case GetJobStatuses(limit) =>
      val infos = jobDao.getJobInfos.values.toSeq.sortBy(_.startTime.toString)
      if (limit.isDefined) {
        sender ! infos.takeRight(limit.get)
      } else {
        sender ! infos
      }

    case GetJobResult(jobId) =>
      breakable {
        val jobInfoOpt = jobDao.getJobInfos.get(jobId)
        if (!jobInfoOpt.isDefined) {
          sender ! NoSuchJobId
          break()
        }

        jobInfoOpt.filter { job => job.isRunning || job.isErroredOut }
          .foreach { jobInfo =>
          sender ! jobInfo
          break()
        }

        // get the context from jobInfo
        val context = jobInfoOpt.get.contextName

        val future = (contextSupervisor ? ContextSupervisor.GetResultActor(context)).mapTo[ActorRef]
        val resultActor = Await.result(future, 3 seconds)

        val receiver = sender() // must capture the sender since callbacks are run in a different thread
        for (result <- resultActor ? GetJobResult(jobId)) {
          receiver ! result // a JobResult(jobId, result) object is sent
        }
      }

    case GetJobConfig(jobId) =>
      sender ! jobDao.getJobConfigs.getOrElse(jobId, NoSuchJobId)

    case StoreJobConfig(jobId, jobConfig) =>
      jobDao.saveJobConfig(jobId, jobConfig)
      sender ! JobConfigStored
  }
}
