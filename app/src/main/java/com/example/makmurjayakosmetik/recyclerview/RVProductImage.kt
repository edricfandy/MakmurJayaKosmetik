package com.example.makmurjayakosmetik.recyclerview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.google.android.material.button.MaterialButton

class RVProductImage(val activity: FragmentActivity, val mode: String, val listImage : ArrayList<String>) : RecyclerView.Adapter<RVProductImage.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.findViewById<RelativeLayout>(R.id.layoutProductImageView_container)
        val slideshowImage = itemView.findViewById<ImageView>(R.id.layoutProductImageView_imageViewSlideShow)
        val msgSlideshowImageUnavailable = itemView.findViewById<TextView>(R.id.layoutProductImageView_msgUnavailableImageSlideshow)
        val layoutAddImage = itemView.findViewById<ConstraintLayout>(R.id.layoutProductImageView_layoutAddImage)
        val addImageView = itemView.findViewById<ImageView>(R.id.layoutProductImageView_imageViewAddImage)
        val txtImageView = itemView.findViewById<TextView>(R.id.layoutProductImageView_txtImage)
        val msgPreviewImageUnavailable = itemView.findViewById<TextView>(R.id.layoutProductImageView_msgUnavailableImageAddImage)
        val btnAddImage = itemView.findViewById<MaterialButton>(R.id.layoutProductImageView_btnAddPic)
        val btnRemoveImage = itemView.findViewById<ImageView>(R.id.layoutProductImageView_btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_product_image_view, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val imagePath = listImage[position]

            if (mode == "slideshow") {
                layoutAddImage.visibility = View.GONE
                if (imagePath.isEmpty()) {
                    msgSlideshowImageUnavailable.visibility = View.VISIBLE
                    msgSlideshowImageUnavailable.text = itemView.context.getString(R.string.no_pic)
                } else {
                    val bitmap: Bitmap
                    try {
                        bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(itemView.context.contentResolver, imagePath.toUri()))
                        else
                            MediaStore.Images.Media.getBitmap(itemView.context.contentResolver, imagePath.toUri())
                        slideshowImage.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        msgSlideshowImageUnavailable.visibility = View.VISIBLE
                    }
                }
                itemView.setOnClickListener {

                }
            } else {
                container.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                slideshowImage.visibility = View.GONE
                txtImageView.text = "Image ${layoutPosition + 1}"
                btnAddImage.visibility = View.VISIBLE
                btnRemoveImage.visibility = View.GONE
                addImageView.setImageURI(Uri.parse(imagePath))
                if (addImageView.drawable != null) {
                    btnRemoveImage.visibility = View.VISIBLE
                    btnAddImage.visibility = View.GONE
                }

                btnAddImage.setOnClickListener { it ->
                    val menu = PopupMenu(itemView.context, it)
                    menu.menuInflater.inflate(R.menu.menu_choose_image_from, menu.menu)
                    menu.setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.menuChooseImageFrom_openCamera -> {
                                val intent = Intent("OPEN_CAMERA")
                                itemView.context.sendBroadcast(intent)
                                true
                            }
                            R.id.menuChooseImageFrom_openGallery -> {
                                val intent = Intent("OPEN_GALLERY")
                                itemView.context.sendBroadcast(intent)
                                true
                            }
                            else -> false
                        }
                    }
                    menu.show()
                }

                btnRemoveImage.setOnClickListener {
                    val dialog = AlertDialog.Builder(itemView.context)
                        .setTitle("Remove Image")
                        .setMessage("Are you sure want to remove this image?")
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton("CONFIRM") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                            listImage.removeAt(layoutPosition)
                            notifyItemRemoved(layoutPosition)
                            notifyDataSetChanged()
                        }
                        .setNegativeButton("CANCEL") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }

                    dialog.show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listImage.size
    }
}