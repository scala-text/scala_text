import org.scalacheck._, Arbitrary.arbitrary
object Testing {
  def test[G](g: Gen[G])(f: G  => Boolean) = {
    val result = Prop.forAll(g)(f).apply(Gen.Parameters.default)
    assert(result.success, result)
  }
}
