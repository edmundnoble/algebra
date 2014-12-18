package algebra
package ring

import scala.{ specialized => sp }

import java.lang.Double.{ isInfinite, isNaN, doubleToLongBits }
import java.lang.Long.{ numberOfTrailingZeros }

trait Field[@sp(Byte, Short, Int, Long, Float, Double) A] extends EuclideanRing[A] with MultiplicativeCommutativeGroup[A] {

  /**
   * This is implemented in terms of basic Field ops. However, this is
   * probably significantly less efficient than can be done with a
   * specific type. So, it is recommended that this method be
   * overriden.
   *
   * This is possible because a Double is a rational number.
   */
  def fromDouble(a: Double): A =
    if (a == 0.0) zero else if (a.isValidInt) fromInt(a.toInt) else {
      require(!isInfinite(a) && !isNaN(a), "Double must be representable as a fraction.")

      val bits = doubleToLongBits(a)
      val m = bits & 0x000FFFFFFFFFFFFFL | 0x0010000000000000L
      val zeros = numberOfTrailingZeros(m)
      val value = m >>> zeros
      val exp = ((bits >> 52) & 0x7FF).toInt - 1075 + zeros // 1023 + 52

      val high = times(fromInt((value >>> 30).toInt), fromInt(1 << 30))
      val low = fromInt((value & 0x3FFFFFFF).toInt)
      val num = plus(high, low)
      val unsigned = if (exp > 0) {
        times(num, pow(fromInt(2), exp))
      } else if (exp < 0) {
        div(num, pow(fromInt(2), -exp))
      } else {
        num
      }

      if (a < 0) negate(unsigned) else unsigned
    }
}

trait FieldFunctions extends EuclideanRingFunctions with MultiplicativeGroupFunctions {
  def fromDouble[@sp(Byte, Short, Int, Long, Float, Double) A](n: Double)(implicit ev: Field[A]): A =
    ev.fromDouble(n)
}

object Field extends FieldFunctions {
  @inline final def apply[A](implicit ev: Field[A]): Field[A] = ev
}
