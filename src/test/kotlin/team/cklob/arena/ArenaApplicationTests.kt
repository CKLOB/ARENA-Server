package team.cklob.arena

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ArenaApplicationTests : DescribeSpec({
    describe("ArenaApplication 클래스는") {
        it("Spring 컨텍스트를 정상적으로 로드한다") {
        }
    }
}) {
    override fun extensions(): List<Extension> = listOf(SpringExtension)
}
