package com.example.navigationapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BlankFragment3 : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment3, container, false)

        val btn4: Button = view.findViewById(R.id.button4)
        btn4.setOnClickListener {
            findNavController().navigate(R.id.action_blankFragment3_to_blankFragment6)
        }

        val btn4Back: Button = view.findViewById(R.id.button4_back)
        btn4Back.setOnClickListener {
            findNavController().popBackStack()
        }

        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            "result_from_activity",
            "ответные данные"
        )

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BlankFragment3().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}