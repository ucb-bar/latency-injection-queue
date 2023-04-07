package latqueue

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import chisel3.experimental.BundleLiterals._

class TBundle extends Bundle {
  val a = UInt(4.W)
  val b = UInt(8.W)
}

class LatencyInjectionQueueSpec extends AnyFreeSpec with ChiselScalatestTester {
  "minimal test" in {
    test(new LatencyInjectionQueue(new TBundle, 16)) { dut =>
      dut.io.enq.valid.poke(false.B)
      dut.clock.step()

      dut.io.enq.valid.poke(true.B)
      dut.io.latency_cycles.poke(4.U)
      dut.io.enq.bits.a.poke(1.U)
      dut.io.enq.bits.b.poke(2.U)
      dut.clock.step()

      dut.io.enq.valid.poke(false.B)
      dut.io.deq.valid.expect(false.B)
      dut.clock.step()

      dut.io.deq.valid.expect(false.B)
      dut.clock.step()

      dut.io.deq.valid.expect(false.B)
      dut.clock.step()

      dut.io.deq.ready.poke(false.B)
      dut.io.deq.valid.expect(true.B)
      dut.io.deq.bits.a.expect(1.U)
      dut.io.deq.bits.b.expect(2.U)
      dut.clock.step()

      dut.io.deq.ready.poke(true.B)
      dut.io.deq.valid.expect(true.B)
      dut.clock.step()

      dut.io.deq.valid.expect(false.B)
    }
  }
}
