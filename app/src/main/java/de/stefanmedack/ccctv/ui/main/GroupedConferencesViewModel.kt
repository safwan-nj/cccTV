package de.stefanmedack.ccctv.ui.main

import android.arch.lifecycle.ViewModel
import de.stefanmedack.ccctv.model.Resource
import de.stefanmedack.ccctv.persistence.entities.ConferenceWithEvents
import de.stefanmedack.ccctv.repository.ConferenceRepository
import io.reactivex.Flowable
import javax.inject.Inject

class GroupedConferencesViewModel @Inject constructor(
        private val repository: ConferenceRepository
) : ViewModel() {

    lateinit var conferenceGroup: String

    fun init(conferenceGroup: String) {
        this.conferenceGroup = conferenceGroup
    }

    val conferencesWithEvents: Flowable<Resource<List<ConferenceWithEvents>>>
        get() = repository.loadedConferences(conferenceGroup)
                .map<Resource<List<ConferenceWithEvents>>> {
                    if (it is Resource.Success)
                        Resource.Success(it.data.sortedByDescending { it.conference.title })
                    else
                        it
                }

}