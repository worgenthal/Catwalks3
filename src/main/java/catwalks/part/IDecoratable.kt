package catwalks.part

import catwalks.block.EnumDecoration

/**
 * Created by TheCodeWarrior
 */
interface IDecoratable {
    fun addDecoration(decor: EnumDecoration): Boolean
    fun removeDecoration(decor: EnumDecoration): Boolean
    fun hasDecoration(decor: EnumDecoration): Boolean
}
