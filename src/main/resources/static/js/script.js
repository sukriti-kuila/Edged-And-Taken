const toggleSidebar = () => {
  const sidebar = document.querySelector(".sidebar");
  const content = document.querySelector(".content");
  
  if (sidebar.style.display === "block") {
    sidebar.style.display = "none";
    content.style.marginLeft = "0%";
  } else {
    sidebar.style.display = "block";
    content.style.marginLeft = "20%";
  }
}

// searching blogs
function searchBlogs() {
  const normalBlogs = document.getElementById('blogs-container');	
  const searchInput = document.getElementById('searchInput');
  const filter = searchInput.value.toUpperCase();
  const blogCards = document.querySelectorAll('.blog-card');
  //where searhed elements will be shown (#searchResults)
  const searchResults = document.getElementById('searchResults');
  searchResults.innerHTML = ''; // clear previous results
  console.log(filter);
  //after searching something if someone searchs something empty
  if (filter == '' && normalBlogs.style.display=='none')
  {
    normalBlogs.style.display='';
    console.log("First");
  }
  if(filter !='')
  {
	  blogCards.forEach((blogCard) => 
    {
	    const title = blogCard.dataset.title.toUpperCase();
	    if (title.includes(filter)) 
      {
			console.log("Second");
	      // insert matching blog card into search results container
        // blogCard.style.display = 'none';
	      searchResults.insertAdjacentElement('afterbegin', blogCard.parentElement.parentElement.cloneNode(true));
	      normalBlogs.style.display='none';
	    } 
      else 
      {
	      blogCard.style.display = '';
	    }
	  });
  }
}
// function searchBlogs() {
//   const searchInput = document.getElementById('searchInput');
//   const filter = searchInput.value.toUpperCase();
//   const blogCards = document.querySelectorAll('.blog-card');
//   const searchResults = document.getElementById('searchResults');
//   searchResults.innerHTML = ''; // clear previous results
//   blogCards.forEach((blogCard) => {
//     const title = blogCard.dataset.title.toUpperCase();
//     if (title.includes(filter)) {
//       // insert matching blog card into search results container
//       searchResults.insertAdjacentElement('afterbegin', blogCard.parentElement.parentElement.cloneNode(true));
//       blogCard.style.display = 'none';
//     } else {
//       blogCard.style.display = 'none';
//     }
//   });
// }

const paymentStart = () => {
	// var button = document.querySelector('.btn');
  //   var value = button.value;
  //   var title = button.getAttribute('data-title');
  //   console.log('Value:', value);
  //   console.log('Title:', title);
  var buttonId = event.target.id;
  console.log(buttonId);
  console.log("Payment Started");
  // let amount = document.getElementById("payment_field").value;
  let blogid = document.getElementById(buttonId).value;
  let amount = 10;
  console.log(blogid);
  if (amount === "" || amount === null) {
    swal("Opps!", "Enter an amount!", "error");
    return;
  }
  fetch("/user/create_order", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ amount: amount, info: "order_request",bid:blogid})
  })
  .then(response => response.json())
  .then(data => {
    console.log("data coming from client "+data);
    if (data.status === 'created') {
      //open payment form
      let options = {
        key: 'rzp_test_zCFwgvXFCB7zim',
        amount: data.amount,
        currency: 'INR',
        name: 'Blog Application',
        description: 'Payment For Blog',
        order_id: data.id,
        handler: function(response) {
          console.log(response.razorpay_payment_id);
          console.log(response.razorpay_order_id);
          console.log(response.razorpay_signature);
          console.log("payment successful");
          updatePaymentOnServer(response.razorpay_payment_id,
            response.razorpay_order_id,
            "paid",blogid);
          alert("Succesful Payment!");
        },
        //prefill - things will be filled beforehand in the form
        prefill: {
          "name": "",
          "email": "",
          "contact": ""
        },
        notes: {
          "address": "Demo Site"
        },
        theme: {
          "color": "#3399cc"
        }
      };
      const rzp = new Razorpay(options);
      rzp.on('payment.failed', function(response) {
        console.log(response.error.code);
        console.log(response.error.description);
        console.log(response.error.source);
        console.log(response.error.step);
        console.log(response.error.reason);
        console.log(response.error.metadata.order_id);
        console.log(response.error.metadata.payment_id);
        alert("Opps!", "Payment Failed!", "error");
      });
      rzp.open();
    }
  })
  .catch(error => console.log(error));
};

function updatePaymentOnServer(payment_id, order_id, status, blogid) {
  fetch('/user/update_order', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ payment_id: payment_id, order_id: order_id, status: status, blogid: blogid })
  })
  .then(function(response) {
    if (response.ok) {
	alert(blogid)
      window.location.href = "/user/premium/read/"+blogid;
    } else {
      alert("Opps! Payment is successful but there is some server-side error!");
    }
  })
  .catch(function(error) {
    alert("Server side error but payment is successful")
  });
}

  
           



