package com.example.ketxe.view.home

interface ActivityPresenter {
    fun onStart()
    fun onResume(time: Int)
    fun onPause(time: Int)
    fun onNoLongerVisible()
    fun onDestroyBySystem()
}

interface HomePresenter {

}

class HomePresenterImpl: HomePresenter {

}