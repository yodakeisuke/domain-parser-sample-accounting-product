package product

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import com.github.michaelbull.result.Ok

class SmokeTest : StringSpec({
    "basic test" {
        val result = Ok(1)
        result.value shouldBe 1
    }
}) 