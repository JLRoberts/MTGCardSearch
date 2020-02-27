package com.babblingbrook.mtgcardsearch.ui.search

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.babblingbrook.mtgcardsearch.R
import com.babblingbrook.mtgcardsearch.data.NetworkState
import com.babblingbrook.mtgcardsearch.model.Card
import com.babblingbrook.mtgcardsearch.util.DebouncingQueryTextListener
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.rv_card_item.*
import javax.inject.Inject

class SearchFragment : Fragment(), SearchAdapter.OnClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var viewModel: SearchViewModel

    private val searchResultAdapter = SearchAdapter(this)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rv_cards.layoutManager = LinearLayoutManager(requireContext())
        rv_cards.addItemDecoration(DividerItemDecoration(requireContext(),
            LinearLayoutManager.VERTICAL))
        rv_cards.adapter = searchResultAdapter

        viewModel.networkState?.observe(viewLifecycleOwner, Observer {
            searchResultAdapter.updateNetworkState(it)
        })

        viewModel.cards.observe(viewLifecycleOwner, Observer {
            searchResultAdapter.submitList(it)
        })

        search_field.doOnTextChanged { text, _, _, _ ->
            viewModel.search(text.toString())
        }
    }

    override fun onCardRowClicked(view: View, card: Card?) {
        val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(card)
        this.findNavController().navigate(action)
    }

    override fun whenListIsUpdated(size: Int, networkState: NetworkState?) {
        setInitialStates()
        if (size == 0) {
            when (networkState) {
                NetworkState.FAILED -> {
                    fragment_text_network.text = getString(R.string.error_msg)
                    fragment_text_network.visibility = View.VISIBLE
                }
                NetworkState.RUNNING -> {
                    fragment_progress_bar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setInitialStates() {
        fragment_text_network.visibility = View.GONE
        fragment_progress_bar.visibility = View.GONE
    }
}
