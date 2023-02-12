package com.wechantloup.gamelistoptimization.usecase

import com.wechantloup.gamelistoptimization.data.AccountRepository

class AccountUseCase(private val repo: AccountRepository) {

    fun getAccount(): Pair<String, String> {
        return repo.getAccount()
    }

    fun saveAccount(login: String, password: String) {
        repo.saveAccount(login, password)
    }
}
