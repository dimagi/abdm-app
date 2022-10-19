package org.commcare.dalvik.domain.usecases

import org.commcare.dalvik.domain.repositories.AbdmRepository
import javax.inject.Inject

abstract class BaseUseCase(@Inject val abdmRepository: AbdmRepository) {
}