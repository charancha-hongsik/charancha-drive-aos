data class WinRewardHistoryResponse(
    val items: MutableList<Item?>,
    val meta:Meta,
    val links:Links
)

data class Item(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val box: Box,
    val isWin: Boolean,
    val expiredAt: String?,
    val item: ItemDetail?,
    val edit: Edit?,
    val userDelivery: UserDelivery?
)

data class Box(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val user: User,
    val userId: String,
    val reward: Reward,
    val rewardId: String,
    val status: String,
    val statusId: String,
    val data: BoxData,
    val result: Result?
)

data class User(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val email: String,
    val isPrivateEmail: Boolean,
    val provider: String,
    val providerId: String,
    val firstName: String,
    val lastName: String,
    val identifier: String,
    val profile: Profile,
    val accountAddress: String
)


data class Profile(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val nickName: String,
    val user: User,
    val userId: String,
    val image: File?,
    val imageId: String?
)

data class File(
    val id: String,
    val bucket: String,
    val createdAt: String,
    val key: String,
    val mimetype: String,
    val name: String,
    val originalName: String,
    val path: String,
    val size: Int
)

data class Reward(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val isActive: Boolean,
    val type: String,
    val typeId: String,
    val data: RewardData,
    val parent: String?,
    val parentId: String?
)

data class RewardData(
    val maxAllowedBoxes: Int,
    val standardDistance: Int
)

data class LocalizationText(
    val en: String
)

data class BoxData(
    val rewardDistance: Int
)

data class Result(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val box: Box,
    val boxId: String,
    val isWin: Boolean,
    val expiredAt: String?,
    val item: ItemDetail,
    val itemId: String,
    val data: ResultData,
    val edit: Edit?,
    val userDelivery: UserDelivery?
)

data class ItemDetail(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val editor: Editor,
    val editorId: String,
    val category: String, // String?
    val categoryId: String,
    val brand: String,
    val name: String,
    val description: String,
    val probability: Double,
    val price: Int,
    val totalQuantity: Int,
    val rewardedQuantity: Int,
    val startedAt: String,
    val endedAt: String,
    val files: List<File?>,
    val parent: String?,
    val parentId: String?
)

data class Editor(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val email: String,
    val isActive: Boolean,
    val name: String,
    val password: String,
    val permissions: List<String?>,
    val role: String,
    val roleId: String,
    val signinId: String
)

data class ResultData(
    val resultFloat: Int,
    val totalProbability: Int,
    val logs: List<String?>,
    val rewardedOrder: Int
)

data class Edit(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val result: Result,
    val resultId: String,
    val editor: Editor,
    val editorId: String,
    val isDelivered: Boolean,
    val residentNumberVerified: Boolean,
    val taxPaymentConfirmed: Boolean,
    val memo: String?,
    val parent: String?,
    val parentId: String?,
    val files: List<EditFile>?
)

data class EditFile(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val order: Int,
    val edit: String,
    val editId: String,
    val file: File,
    val fileId: String
)

data class UserDelivery(
    val createdAt: String,
    val deletedAt: String?,
    val updatedAt: String,
    val id: String,
    val privacyConsent: Boolean,
    val result: Result,
    val resultId:String,
    val status:String,
    val statusId:String,
    val deliveredAt:String,
    val data:UserDeliveryData,
    val parent:String,
    val parentId:String
)

data class UserDeliveryData(
    val name:String,
    val phone:String,
    val email:String,
    val address:String?
)

data class Meta(
    val totalItems: Int,
    val itemsPerPage: Int,
    val currentPage: Int,
    val totalPages: Int,
    val sort: List<String?>
)

data class Links(
    val current: String,
    val first: String,
    val previous: String?,
    val next: String?,
    val last: String
)
