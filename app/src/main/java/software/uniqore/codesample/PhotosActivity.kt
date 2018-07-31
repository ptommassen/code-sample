package software.uniqore.codesample

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.android.AndroidInjection
import software.uniqore.codesample.databinding.PhotoBinding
import software.uniqore.codesample.databinding.PhotosActivityBinding
import software.uniqore.codesample.model.Photo
import software.uniqore.codesample.support.GlideApp
import javax.inject.Inject

open class PhotosActivity : AppCompatActivity() {
    @Inject
    @VisibleForTesting
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: PhotosActivityBinding
    private lateinit var viewModel: PhotoViewModel

    open fun inject() {
        AndroidInjection.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        binding = DataBindingUtil.setContentView(this, R.layout.photos_activity)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PhotoViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()

        binding.swiperefresh.setOnRefreshListener { viewModel.refreshPhotos() }

        binding.myRecyclerView.layoutManager = LinearLayoutManager(this)
        val viewAdapter = MyAdapter(arrayListOf())
        binding.myRecyclerView.adapter = viewAdapter
        viewModel.photos.observe(this, Observer<List<Photo>> {
            it?.let {
                binding.swiperefresh.isRefreshing = false
                viewAdapter.setPhotos(it)
            }
        })

        viewModel.loading.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                binding.swiperefresh.isRefreshing = viewModel.loading.get()
            }
        })

    }

    override fun onStart() {
        super.onStart()
        viewModel.loadPhotos()
    }


    class MyAdapter(private var photos: List<Photo>) :
            RecyclerView.Adapter<MyAdapter.ViewHolder>() {


        class ViewHolder(val itemBinding: PhotoBinding) : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): MyAdapter.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemBinding = PhotoBinding.inflate(inflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemBinding.apply {
                photo = photos[position]
                GlideApp.with(image).load(photos[position].url).centerCrop().into(image)
                executePendingBindings()
            }
        }

        override fun getItemCount() = photos.size

        fun setPhotos(photos: List<Photo>) {
            this.photos = photos
            notifyDataSetChanged()
        }

    }

}