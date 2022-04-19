import {h, Component, render} from 'https://unpkg.com/preact?module';
import htm from 'https://unpkg.com/htm?module';

const url = `/rest/ignore/shows`;
// Initialize htm with Preact
const html = htm.bind(h);

class ListShow extends Component {
	state = {values: []};
	
	listShows = () => {
		axios
			.get(url)
			.then(response => response.data)
			.then((values) => {
				this.setState({ values });
			})
	}
	
	deleteShow = id => {
		axios
			.delete(url + "/" + id)
			.then(() => this.listShows())
			.catch(function (error) {
				console.error(error);
			});
	}

	render(_, {values}) {
		return html`
			<button class="matter-button-outlined" onClick=${this.listShows}>List shows</button>
			<table>
			${values.map((value) =>
				html`<tr>
					<th><a class="matter-link" href="https://lv.houseseats.com/member/tickets/view/?showid=${value.id}">${value.name}</a></th>
					<th><button class="matter-button-outlined" onClick=${() => this.deleteShow(value.id)}>Delete</button></th>
				</tr>`
			)}
			</table>
		`;
	}
}

class AddShow extends Component {
	state = {id: "", name: ""}

	onSubmit = event => {
		axios.post(url, this.state)
			.then(function (response) {
				console.log(response);
			})
			.catch(function (error) {
				console.error(error);
			});
		event.preventDefault();
	};
	onInput = event => {
		let {value} = event.target;
		let {id} = event.target;
		this.setState({[id]: value})

	};
	render(_, {value}) {
		let {name, id} = this.state;
		return html`
			<form onSubmit=${this.onSubmit}>
				<label class="matter-textfield-standard" for="id">
					<input type="number" value=${id} id="id" onInput=${this.onInput} />
					<span>Identification</span>
				</label><br />
				<label class="matter-textfield-standard" for="name">
					<input type="text" value=${name} id="name" onInput=${this.onInput} />
					<span>Name</span>
				</label><br />
				<button class="matter-button-outlined" type="submit">Add</button>
			</form>
		`;
	}
}

function App() {
	return html`
		<p class="matter-body1">Add a show that should not be alerted of new offerings.</p>
		<${AddShow} />
		<hr />
		<p class="matter-body1">List shows that are not being alerted of new offerings.</p>
		<${ListShow} />
	`;
}

render(html`<${App} />`, document.body);